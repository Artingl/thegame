package dev.artingl.Engine.renderer.pipeline;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.IEngineEvent;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class PipelineManager implements IEngineEvent {
    private final Logger logger;

    private final List<PipelineInstance> pipelineList;
    private final RenderContext renderContext;

    public PipelineManager(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.pipelineList = new ArrayList<>();
        this.renderContext = new RenderContext(logger, renderer);
    }

    public void init() throws EngineException {
        Engine.getInstance().subscribeEngineEvents(this);
    }

    public void cleanup() {
        for (PipelineInstance instance : pipelineList) {
            instance.cleanup();
        }
        Engine.getInstance().unsubscribeEngineEvents(this);
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

    @Override
    public void onReload() {
        this.logger.log(LogLevel.INFO, "Reloading all pipeline instances");

        // Cleanup an initialize (they'll initialize themselves on next render pass)
        for (PipelineInstance instance : pipelineList) {
            instance.cleanup();
        }
    }
}
