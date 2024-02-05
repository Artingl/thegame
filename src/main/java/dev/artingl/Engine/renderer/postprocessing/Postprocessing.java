package dev.artingl.Engine.renderer.postprocessing;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.pipeline.IPipeline;
import dev.artingl.Engine.renderer.pipeline.PipelineInstance;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.GL_LINEAR;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL30C.glDeleteTextures;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class Postprocessing implements IPipeline {
    private final Logger logger;

    private final ShaderProgram postprocessingShader;
    private BaseMesh screenMesh;
    private int fbo, textureId;

    private final List<IPostprocess> effects;

    public Postprocessing(Logger logger) {
        this.logger = logger;

        this.postprocessingShader = new ShaderProgram(
                new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/postprocessing/postprocessing.vert")),
                new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/postprocessing/postprocessing.frag"))
        );

        this.effects = new ArrayList<>();
    }

    /**
     * Add post-processing effect
     * */
    public void addEffect(IPostprocess effect) {
        this.effects.add(effect);
    }

    public int getFramebuffer() {
        return fbo;
    }

    public int getTextureId() {
        return textureId;
    }

    public void render(RenderContext renderContext, ShaderProgram shaderProgram) {
        this.screenMesh.setShaderProgram(shaderProgram);
        shaderProgram.setMainTexture(Texture.MISSING);
        this.screenMesh.render(renderContext, GL_QUADS);
    }

    @Override
    public void pipelineCleanup(PipelineInstance instance) {
        for (IPostprocess effect: effects) {
            effect.cleanup(this);
        }

        this.effects.clear();
        this.screenMesh.cleanup();
        this.postprocessingShader.cleanup();

        glDeleteFramebuffers(this.fbo);
        glDeleteTextures(this.textureId);
    }

    @Override
    public void pipelineInit(PipelineInstance instance) throws EngineException {
        this.screenMesh = new BaseMesh(
                new Vector3f(), new Vector3f(),
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(-1.0f, -1.0f, 0.0f)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(1.0f, -1.0f, 0.0f)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(1.0f, 1.0f, 0.0f)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-1.0f, 1.0f, 0.0f)).addAttribute(new Vector2f(0, 1))
        );
        this.screenMesh.bake();

        this.postprocessingShader.bake();
        this.postprocessingShader.addTextureUniform("postprocessingFramebufferTexture", 2);

        // Init framebuffer and texture for the bloom shader
        Display display = Engine.getInstance().getDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        this.fbo = glGenFramebuffers();
        this.textureId = glGenTextures();

        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);

        glBindTexture(GL_TEXTURE_2D, this.textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, this.textureId, 0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT1});

        // Check framebuffer
        int status;
        if ((status = glCheckFramebufferStatus(GL_FRAMEBUFFER)) != GL_FRAMEBUFFER_COMPLETE)
            throw new EngineException("Unable to make framebuffer for the postprocessing: " + status);

        for (IPostprocess effect: effects) {
            effect.init(this);
        }
    }

    @Override
    public void pipelineRender(RenderContext renderContext, PipelineInstance instance) {
        // Check if the rendering of effects is enabled
        if (renderContext.getRenderer().isPostprocessingEnabled()) {
            // Render all effects
            for (IPostprocess effect : effects) {
                effect.render(this, renderContext);
            }
        }

        this.render(renderContext, this.postprocessingShader);
    }

    @Override
    public int pipelineFlags() {
        return Flags.RENDER_DIRECTLY;
    }
}
