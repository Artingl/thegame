package dev.artingl.Game.registries;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.renderer.scene.BaseScene;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScenesRegistry {
    private final Map<Resource, BaseScene> scenes;

    private BaseScene currentScene;
    private Resource currentSceneName;

    public ScenesRegistry() {
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
     * Switch between scenes
     *
     * @param key The target scene's name
     *
     * @return True if scene was found and successfully switched to, otherwise false
     * */
    public boolean switchScene(Resource key) {
        BaseScene scene = getScene(key);
        Engine engine = Engine.getInstance();

        if (scene != null) {
            engine.getSceneManager().switchScene(scene);

            currentScene = scene;
            currentSceneName = key;
            return true;
        }

        return false;
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
}
