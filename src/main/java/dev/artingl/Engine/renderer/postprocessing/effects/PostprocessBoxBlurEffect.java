package dev.artingl.Engine.renderer.postprocessing.effects;

import dev.artingl.Engine.renderer.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class PostprocessBoxBlurEffect extends PostprocessEffect {
    public static final Shader SHADER = new Shader(
            ShaderType.FRAGMENT,
            new Resource("engine", "shaders/postprocess/effects/boxblur/boxblur.glsl"));

    public PostprocessBoxBlurEffect() {
        super();
        this.setProperty("blurKernel", 2);
    }

    @Override
    public PostprocessEffect[] getPreEffects() {
        return null;
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
