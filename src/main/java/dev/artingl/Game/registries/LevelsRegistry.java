package dev.artingl.Game.registries;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LevelsRegistry {
    private final Map<Resource, Level> levels;
    private Level currentLevel;

    public LevelsRegistry() {
        this.levels = new ConcurrentHashMap<>();
    }

    /**
     * Register level in the game
     *
     * @param key Level's key
     * @param level Level to register
     * */
    public void registerLevel(Resource key, Level level) {
        this.levels.put(key, level);
    }

    /**
     * Get Map of all registered levels
     * */
    public Map<Resource, Level> getLevels() {
        return this.levels;
    }

    /**
     * Get Level by its key
     *
     * @param key Target Level's key
     * */
    @Nullable
    public Level getLevel(Resource key) {
        return this.levels.get(key);
    }

    /**
     * Switch between Level
     *
     * @param key The target Level's key
     *
     * @return True if Level was found and successfully switched to, otherwise false
     * */
    public boolean switchLevel(Resource key) {
        Engine engine = Engine.getInstance();
        Level level = this.levels.get(key);

        if (this.currentLevel != null) {
            // Cleanup current level
            engine.getTimer().unsubscribe(this.currentLevel);
        }

        if (level != null) {
            this.currentLevel = level;

            // Initialize new level
            engine.getTimer().subscribe(this.currentLevel);
            return true;
        }

        return false;
    }

    /**
     * Get current active scene
     * */
    public Level getCurrentLevel() {
        return currentLevel;
    }
}
