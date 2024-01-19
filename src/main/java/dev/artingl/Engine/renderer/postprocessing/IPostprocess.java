package dev.artingl.Engine.renderer.postprocessing;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;

public interface IPostprocess {
    void init(Postprocessing postprocess) throws EngineException;

    void render(Postprocessing postprocess, RenderContext context);

    void cleanup(Postprocessing postprocess);
}
