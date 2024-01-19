package dev.artingl.Engine.renderer.postprocessing;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class Bloom implements IPostprocess {

    private final ShaderProgram bloomShaderA;
    private final ShaderProgram bloomShaderB;


    public Bloom() {
        this.bloomShaderA = new ShaderProgram(
                new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/postprocessing/bloom.vert")),
                new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/postprocessing/bloomA.frag"))
        );
        this.bloomShaderB = new ShaderProgram(
                new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/postprocessing/bloom.vert")),
                new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/postprocessing/bloomB.frag"))
        );
    }


    @Override
    public void cleanup(Postprocessing postprocess) {
        this.bloomShaderA.cleanup();
        this.bloomShaderB.cleanup();
    }

    @Override
    public void init(Postprocessing postprocess) throws EngineException {
        this.bloomShaderA.bake();
        this.bloomShaderB.bake();

        // Add texture that will be used to make the glow effect in the B shader
        this.bloomShaderB.addTextureUniform("postprocessingFramebufferTexture", postprocess.getTextureId());
    }

    @Override
    public void render(Postprocessing postprocess, RenderContext renderContext) {
        Renderer renderer = renderContext.getRenderer();

        // Render first buffer (blurry one) for the bloom
        renderer.bindFramebuffer(postprocess.getFramebuffer());
        postprocess.render(renderContext, this.bloomShaderA);

        // Render second buffer that does the bloom effect
        renderer.bindFramebuffer(0);
        postprocess.render(renderContext, this.bloomShaderB);
    }
}
