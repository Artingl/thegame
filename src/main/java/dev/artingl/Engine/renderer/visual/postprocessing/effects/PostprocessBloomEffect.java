package dev.artingl.Engine.renderer.visual.postprocessing.effects;

import dev.artingl.Engine.renderer.visual.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class PostprocessBloomEffect extends PostprocessEffect {

    public static final Shader SHADER = new Shader(
            ShaderType.FRAGMENT,
            new Resource("engine", "shaders/postprocess/effects/bloom/bloom.glsl"));

    @Override
    public PostprocessEffect[] initPreEffects() {
        PostprocessEffect[] effects = new PostprocessEffect[2];

        for (int i = 0; i < effects.length; i++) {
            PostprocessBoxBlurEffect blur = new PostprocessBoxBlurEffect();
            blur.setProperty("kernel", 2 * (1 + i));
            effects[i] = blur;
        }

        return effects;
    }

    @Override
    public String[] getTexUniforms() {
        return new String[]{};
    }

    @Override
    public Shader[] getShaders() {
        return new Shader[]{SHADER};
    }

    @Override
    public void prepareRender() {

    }
}
