package dev.artingl.Engine.renderer.visual.postprocessing;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.Framebuffer;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.*;

public class PostprocessManager {
    public static final ShaderProgram POSTPROCESSING_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/postprocess/postprocessing.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/postprocess/postprocessing.frag"))
    );

    public static final Shader EFFECT_VERT = new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/postprocess/effects/effect.vert"));
    private static final int MODE = GL_TRIANGLES;

    private final Logger logger;
    private final List<EffectInstance> effects;
    private final List<PostprocessEffect> lazyEffects;
    private final Framebuffer[] framebuffers;
    private int bufferId;
    private BaseMesh screenQuad;

    public PostprocessManager(Logger logger) {
        this.effects = new ArrayList<>();
        this.lazyEffects = new ArrayList<>();
        this.framebuffers = new Framebuffer[]{ new Framebuffer(), new Framebuffer() };
        this.logger = logger;
    }

    /**
     * Add post-processing effect
     * */
    public void addEffect(PostprocessEffect effect) {
        synchronized (this.lazyEffects) {
            this.lazyEffects.add(effect);
        }
    }

    public Collection<PostprocessEffect> getEffects() {
        Collection<PostprocessEffect> effects = new ArrayList<>();
        for (EffectInstance inst: this.effects)
            effects.add(inst.effect);

        return effects;
    }

    public Framebuffer getFramebuffer() {
        return framebuffers[bufferId++ % 2];
    }

    public void cleanup() {
        for (EffectInstance inst: effects) {
            inst.cleanup(this);
        }

        this.lazyEffects.clear();
        this.effects.clear();
        this.screenQuad.cleanup();
        this.framebuffers[0].cleanup();
        this.framebuffers[1].cleanup();
    }

    public void init() throws EngineException {
        if (this.screenQuad != null && this.screenQuad.isBaked())
            return;

        this.screenQuad = new BaseMesh(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(-1.0f, -1.0f, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(1.0f, -1.0f, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-1.0f, 1.0f, 0)).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f(1.0f, 1.0f, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-1.0f, 1.0f, 0)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(1.0f, -1.0f, 0)).addAttribute(new Vector2f(1, 0))
        );
        this.screenQuad.bake();
        this.screenQuad.enableFade(false);
        POSTPROCESSING_PROGRAM.bake();
        this.framebuffers[0].init();
        this.framebuffers[1].init();
    }

    public void render(Renderer renderer) throws EngineException {
        if (this.framebuffers[0].updateBuffer() || this.framebuffers[1].updateBuffer())
            return;

        Framebuffer mainFb = renderer.getMainFramebuffer();
        Viewport viewport = renderer.getViewport().getCurrentViewport();
        if (viewport == null)
            return;

        renderer.bindFramebuffer(null);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Clear buffers
        this.framebuffers[0].clear(renderer, Color.BLACK);
        this.framebuffers[1].clear(renderer, Color.BLACK);

        // Render initial framebuffer to postprocessing framebuffer to work with it later
        this.bufferId = 0;
        renderer.bindFramebuffer(getFramebuffer());
        POSTPROCESSING_PROGRAM.setTextureUniform("ppTex", mainFb.getFrameTexture());
        this.screenQuad.setShaderProgram(POSTPROCESSING_PROGRAM);
        this.screenQuad.render(renderer, MODE);

        // Check if the rendering of effects is enabled
        if (viewport.isPostprocessingEnabled()) {
            // Initialize all effects which are waiting for it
            synchronized (this.lazyEffects) {
                for (PostprocessEffect effect: this.lazyEffects) {
                    EffectInstance inst = new EffectInstance(effect);
                    inst.init(this);
                    this.effects.add(inst);
                }

                this.lazyEffects.clear();
            }

            // Render all effects
            for (EffectInstance inst: effects) {
                if (inst.effect.isEnabled())
                    inst.render(renderer, this, this.screenQuad);
            }
        }
        this.bufferId++;

        // Render the result to the screen
        renderer.bindFramebuffer(null);
        POSTPROCESSING_PROGRAM.setTextureUniform("ppTex", getFramebuffer().getFrameTexture());
        this.screenQuad.setShaderProgram(POSTPROCESSING_PROGRAM);
        this.screenQuad.render(renderer, MODE);

        // Render UI
        POSTPROCESSING_PROGRAM.setTextureUniform("ppTex", renderer.getUiFramebuffer().getFrameTexture());
        this.screenQuad.setShaderProgram(POSTPROCESSING_PROGRAM);
        this.screenQuad.render(renderer, MODE);

        if (renderer.isWireframeEnabled())
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    private static class EffectInstance {
        private final PostprocessEffect effect;
        private final List<EffectInstance> children;
        private ShaderProgram[] programs;

        public EffectInstance(PostprocessEffect effect) {
            this.effect = effect;
            this.children = new ArrayList<>();
        }

        public void init(PostprocessManager postprocess) {
            // Initialize shader programs for the effect
            Shader[] shaders = effect.getShaders();
            this.programs = new ShaderProgram[shaders.length];

            for (int i = 0; i < shaders.length; i++) {
                ShaderProgram program = new ShaderProgram(
                        EFFECT_VERT,
                        shaders[i]
                );
                program.bake();
                this.programs[i] = program;
            }

            // Initialize children if any
            PostprocessEffect[] children = effect.initPreEffects();
            if (children != null) {
                for (PostprocessEffect child: children) {
                    EffectInstance inst = new EffectInstance(child);
                    inst.init(postprocess);
                    this.children.add(inst);
                }
            }
        }

        public void render(Renderer renderer, PostprocessManager postprocess, IMesh screenQuad) {
            // Firstly render all child effects
            for (EffectInstance child: children) {
                child.render(renderer, postprocess, screenQuad);
            }

            // Render the effect itself
            for (ShaderProgram program: programs) {
                effect.prepareRender();
                renderer.bindFramebuffer(postprocess.getFramebuffer());
                program.setTextureUniform("ppTex", postprocess.getFramebuffer().getFrameTexture());
                postprocess.getFramebuffer();
                for (Map.Entry<String, Object> entry: effect.getProperties())
                    program.setUniformObject(entry.getKey(), entry.getValue());
                screenQuad.setShaderProgram(program);
                screenQuad.render(renderer, MODE);
            }
        }

        public void cleanup(PostprocessManager postprocess) {
            for (EffectInstance inst: this.children)
                inst.cleanup(postprocess);
            for (ShaderProgram program: this.programs)
                program.cleanup();
            this.children.clear();
        }

        public PostprocessEffect getEffect() {
            return effect;
        }
    }

}
