package dev.artingl.Engine.world.scene;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.timer.Timer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SceneManager {
    private final Map<Resource, BaseScene> scenes;
    private BaseScene currentScene;
    private Resource currentSceneName;

    public SceneManager() {
        this.scenes = new ConcurrentHashMap<>();
    }

    /**
     * Register scene in the game
     *
     * @param key Scene's key
     * @param scene Scene to register
     * */
    public void registerScene(Resource key, BaseScene scene) {
        this.scenes.put(key, scene);
    }

    /**
     * Get Map of all registered scenes
     * */
    public Map<Resource, BaseScene> getScenes() {
        return this.scenes;
    }

    /**
     * Get Collection of all registered scenes (without keys)
     * */
    public Collection<BaseScene> getSceneValues() {
        return getScenes().values();
    }

    /**
     * Get Collection of keys of all registered scenes
     * */
    public Collection<Resource> getSceneNames() {
        return getScenes().keySet();
    }

    /**
     * Get scene by its key
     *
     * @param key Target scene's key
     * */
    @Nullable
    public BaseScene getScene(Resource key) {
        return this.scenes.get(key);
    }

    /**
     * Get current active scene
     * */
    public BaseScene getCurrentScene() {
        return currentScene;
    }

    /**
     * Key of selected scene
     * */
    public Resource getCurrentSceneName() {
        return currentSceneName;
    }

    /**
     * Switch between scenes
     *
     * @param key The target scene's name
     */
    public void switchScene(Resource key) {
        BaseScene scene = getScene(key);
        if (scene == null)
            return;

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
        this.currentSceneName = key;

        // Subscribe the scene for events
        timer.subscribe(scene);
        input.subscribe(scene);
    }

    @Override
    public String toString() {
        return "ScenesManager{currentScene=" + currentScene + "}";
    }

    public void cleanup() {
    }

    public void init() throws EngineException {
    }

}
