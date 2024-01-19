package dev.artingl.Engine.renderer;

import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.viewport.Viewport;

public class RenderContext {

    private final Logger logger;
    private final Renderer renderer;

    public RenderContext(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.renderer = renderer;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Get the viewport
     * */
    public Viewport getViewport() {
        return this.renderer.getViewport();
    }

}
