package dev.artingl.Game.render.postprocess;

import dev.artingl.Engine.renderer.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;

public class PostprocessPaletteEffect extends PostprocessEffect {
    public static final Shader SHADER = new Shader(
            ShaderType.FRAGMENT,
            new Resource("thegame", "shaders/postprocessing/postprocessing.glsl"));

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
