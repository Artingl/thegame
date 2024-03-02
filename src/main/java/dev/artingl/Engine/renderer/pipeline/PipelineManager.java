package dev.artingl.Engine.renderer.pipeline;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.EngineEventListener;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class PipelineManager implements EngineEventListener {
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
        // Initialize all instances
        for (PipelineInstance instance : pipelineList) {
            instance.getInstance().pipelineInit(instance);
        }
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

            for (PipelineInstance instance : pipelineList) {
                renderer.bindFramebuffer(renderer.getMainFramebuffer());
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

    public RenderContext getRenderContext() {
        return renderContext;
    }

    @Override
    public void onReload() {
        this.logger.log(LogLevel.INFO, "Reloading all pipeline instances");

        // Cleanup all pipeline instances (they'll initialize themselves on next render pass)
        // TODO: fix it...
//        for (PipelineInstance instance : pipelineList) {
//            instance.cleanup();
//        }
    }
}
