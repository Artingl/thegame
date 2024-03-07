package dev.artingl.Engine.renderer.visual.shadow;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class ShadowsManager {
    private final Logger logger;
    private final List<LightSource> lightSources;

    public ShadowsManager(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.lightSources = new ArrayList<>();
    }

    public void cleanup() {

    }

    public void init() throws EngineException {

    }

    public void render(Renderer renderer) throws EngineException {
        // check if shadow mapping is enabled
    }

}
