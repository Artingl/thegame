package dev.artingl.Engine.renderer.postprocessing.effects;

import dev.artingl.Engine.renderer.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class PostprocessBloomEffect extends PostprocessEffect {

    public static final Shader SHADER = new Shader(
            ShaderType.FRAGMENT,
            new Resource("engine", "shaders/postprocess/effects/bloom/bloom.glsl"));

    @Override
    public PostprocessEffect[] getPreEffects() {
        PostprocessBoxBlurEffect blur0 = new PostprocessBoxBlurEffect();
        blur0.setProperty("kernel", 4);

        return new PostprocessEffect[]{ blur0 };
    }

    @Override
    public String[] getTexUniforms() {
        return new String[]{};
    }

    @Override
    public Shader[] getShaders() {
        return new Shader[]{SHADER};
    }
}
