package dev.artingl.Engine.renderer.visual.postprocessing.effects;

import dev.artingl.Engine.renderer.visual.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class PostprocessSSAOEffect extends PostprocessEffect {
    public static final Shader SHADER0 = new Shader(
            ShaderType.FRAGMENT,
            new Resource("engine", "shaders/postprocess/effects/ssao/ssao_pre.glsl"));
    public static final Shader SHADER1 = new Shader(
            ShaderType.FRAGMENT,
            new Resource("engine", "shaders/postprocess/effects/ssao/ssao_post.glsl"));

    public PostprocessSSAOEffect() {
        super();
        this.setProperty("ssaoKernel", 6.0f);
        this.setProperty("ssaoMaxDistance", 100.0f);
        this.setProperty("ssaoBias", 0.000001f);
    }

    @Override
    public Shader[] getShaders() {
        return new Shader[]{ SHADER0, SHADER1 };
    }

}
