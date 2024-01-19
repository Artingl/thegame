package dev.artingl.Engine.renderer.pipeline;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class PipelineManager {
    private final Logger logger;

    private final List<PipelineInstance> pipelineList;
    private final RenderContext renderContext;

    public PipelineManager(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.pipelineList = new ArrayList<>();
        this.renderContext = new RenderContext(logger, renderer);
    }

    public void init() throws EngineException {
    }

    public void cleanup() {
        for (PipelineInstance instance : pipelineList) {
            instance.cleanup();
        }
    }

    /**
     * Remove all elements from the pipeline list
     */
    public void clearPipeline() {
        this.logger.log(LogLevel.INFO, "Clearing the pipeline (%d elems.)", this.pipelineList.size());
        this.pipelineList.clear();
    }

    /**
     * Call the pipeline
     */
    public void call() throws EngineException {
        try {
            Renderer renderer = renderContext.getRenderer();
            boolean isFramebufferBound = false;

            for (PipelineInstance instance : pipelineList) {
                // Setup rendering based on flags
                if ((instance.getInstance().pipelineFlags() & IPipeline.Flags.RENDER_DIRECTLY) != 0) {
                    isFramebufferBound = false;
                    renderer.bindFramebuffer(0);
                } else if (!isFramebufferBound) {
                    isFramebufferBound = true;
                    renderer.bindFramebuffer(renderer.getFramebufferId());
                }

                instance.call(renderContext);
            }
        } catch (EngineException e) {
            logger.exception(e, "Pipeline call error!");
            throw e;
        }
    }

    /**
     * Add interface to the pipeline
     */
    public void append(IPipeline instance) {
        this.pipelineList.add(new PipelineInstance(instance));
    }

    /**
     * Move the element to the end of the pipeline
     * */
    public void makeLast(IPipeline element) {
        PipelineInstance instance = new PipelineInstance(element);
        this.pipelineList.remove(instance);
        this.pipelineList.add(instance);
    }

    /**
     * Get amount of elements in the pipeline
     * */
    public int totalElements() {
        return this.pipelineList.size();
    }
}
