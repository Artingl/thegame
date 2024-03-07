package dev.artingl.Game.render.postprocess;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.visual.postprocessing.PostprocessEffect;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.resources.Resource;

public class PostprocessingEffect extends PostprocessEffect {
    public static final Shader SHADER = new Shader(
            ShaderType.FRAGMENT,
            new Resource("thegame", "shaders/postprocessing/postprocessing.glsl"));

    public PostprocessingEffect() {
        super();
        this.setProperty("vignette", 0.2f);
        this.setProperty("pixelSize", 2);
    }

    @Override
    public PostprocessEffect[] initPreEffects() {
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

    @Override
    public void prepareRender() {
        // A bit of fun here...
        Quality quality = (Quality) Engine.getInstance().getOptions().get(Options.Values.QUALITY_SETTING);
        this.setProperty("pixelSize", quality.equals(Quality.POTATO) ? 10 : 2);
    }
}
