package dev.artingl.Engine.renderer.scene;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.pipeline.IPipeline;
import dev.artingl.Engine.renderer.pipeline.PipelineInstance;
import dev.artingl.Engine.timer.Timer;

public class SceneManager implements IPipeline {
    private BaseScene currentScene;

    public SceneManager() {
    }

    public void switchScene(BaseScene scene) {
        Engine engine = Engine.getInstance();
        Timer timer = engine.getTimer();
        Input input = engine.getInput();

        engine.getLogger().log(LogLevel.INFO, "Switching scenes (" + scene + ")");

        // Tell current scene (if any) that it is going to be hidden
        if (this.currentScene != null) {
            this.currentScene.inactive();

            // Unsubscribe scene from all events
            timer.unsubscribe(this.currentScene);
            input.unsubscribe(this.currentScene);
        }

        // Tell the new scene that it is going to be shown
        scene.active();
        this.currentScene = scene;

        // Subscribe the scene for events
        timer.subscribe(scene);
        input.subscribe(scene);
    }

    public BaseScene getCurrentScene() {
        return currentScene;
    }


    @Override
    public String toString() {
        return "ScenesRenderer{currentScene=" + currentScene + "}";
    }

    @Override
    public void pipelineCleanup(PipelineInstance instance) {
    }

    @Override
    public void pipelineInit(PipelineInstance instance) throws EngineException {
    }

    @Override
    public void pipelineRender(RenderContext renderContext, PipelineInstance instance) throws EngineException {
        if (currentScene != null) {
            currentScene.prepareRender(renderContext);
        }
    }

    @Override
    public int pipelineFlags() {
        return 0;
    }

}
