package dev.artingl.Engine.renderer.pipeline;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;

public interface IPipeline {

    void pipelineCleanup(PipelineInstance instance);
    void pipelineInit(PipelineInstance instance) throws EngineException;
    void pipelineRender(RenderContext renderContext, PipelineInstance instance) throws EngineException;

    int pipelineFlags();

    class Flags {
        public static final int RENDER_DIRECTLY = 1 << 0;

    }

}
