package dev.artingl.Engine.renderer.pipeline;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;

import java.util.ArrayList;
import java.util.List;

public class PipelineInstance {

    private boolean isInitialized;
    private final IPipeline instance;

    private List<PipelineInstance> children;

    public PipelineInstance(IPipeline instance) {
        this.isInitialized = false;
        this.instance = instance;
        this.children = new ArrayList<>();
    }

    /**
     * Call the pipelineRender method of the instance.
     * If the instance is not initialized, the pipelineInit method will be called first
     *
     * @param context Current render context
     * */
    public void call(RenderContext context) throws EngineException {
        // Call children first
        for (PipelineInstance child: children) {
            child.call(context);
        }

        if (!this.isInitialized) {
            this.instance.pipelineInit(this);
            this.isInitialized = true;
        }

        this.instance.pipelineRender(context, this);
    }

    /**
     * Cleanup the instance by calling pipelineCleanup
     * */
    public void cleanup() {
        if (this.isInitialized) {
            // cleanup all children
            for (PipelineInstance child: children) {
                child.cleanup();
            }

            this.instance.pipelineCleanup(this);
        }
        this.isInitialized = false;
    }

    /**
     * Add child pipeline instance that will be rendered before rendering this instance.
     * The child instance will not be added to the main pipeline list, only inside this instance's own list.
     *
     * @param childInstance The child instance to be added.
     * */
    public void addChild(IPipeline childInstance) {
        this.children.add(new PipelineInstance(childInstance));
    }

    public IPipeline getInstance() {
        return instance;
    }

    @Override
    public int hashCode() {
        return instance.getClass().getSimpleName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PipelineInstance))
            return false;
        return ((PipelineInstance)obj).hashCode() == hashCode();
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
