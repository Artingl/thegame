package dev.artingl.Game;

import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.registries.LevelsRegistry;
import dev.artingl.Game.scene.MapScene;
import dev.artingl.Game.registries.ScenesRegistry;
import dev.artingl.Game.level.Level;

public class GameDirector {
    private static GameDirector instance;
    public static GameDirector getInstance() {
        return instance;
    }

    // ---------------

    // Engine
    private final Logger logger;
    private final Engine engine;
    // ---------------

    // Game
    private final ScenesRegistry sceneRegistry;
    private final LevelsRegistry levelsRegistry;

    // ---------------

    private boolean isRunning;

    public GameDirector() {
        GameDirector.instance = this;

        /* Initialize engine */
        this.engine = new Engine("thegame");
        this.engine.enableDebugger();

        this.logger = this.engine.getLogger();

        /* Initialize game related classes */
        this.levelsRegistry = new LevelsRegistry();
        this.sceneRegistry = new ScenesRegistry();

        /* Replace System.err and System.out with our logger */
        System.setOut(this.logger);
        System.setErr(this.logger);

        try {
            /* Initialize the engine.
             * This will create the GL context for current thread,
             * make the window and initialize the renderer with the pipeline
             */
            this.engine.create();
        } catch (Exception e) {
            /* We got critical error.
             * Print it out the error and exit with code 1
             */
            logger.exception(e, "Got critical error!");
            System.exit(1);
        }

        /* Disable vsync */
        this.engine.getDisplay().setVsync(false);

        /* Add post-processing effects */
//        this.engine.getRenderer().getPostprocessing().addEffect(new Bloom());

        /* Register levels */
        this.levelsRegistry.registerLevel(new Resource("thegame", "level/park"), new Level());
        this.levelsRegistry.switchLevel(new Resource("thegame", "level/park"));

        /* Register scenes */
        this.sceneRegistry.registerScene(new Resource("thegame", "scene/map"), new MapScene());
        this.sceneRegistry.switchScene(new Resource("thegame", "scene/map"));
    }

    public int run() {
        this.isRunning = true;
        int exitCode = 0;

        try {
            while (engine.isAlive() && this.isRunning) {
                engine.frame();
            }
        } catch (Exception e) {
            /* We got critical error in the main game loop.
             * Print it out and terminate everything
             */
            logger.exception(e, "Got critical error!");
            exitCode = 1;
        }

        this.engine.terminate();
        return exitCode;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void shutdown() {
        this.isRunning = false;
    }

    public Engine getEngine() {
        return engine;
    }

    public Logger getLogger() {
        return logger;
    }

    public ScenesRegistry getSceneManager() {
        return sceneRegistry;
    }

    public LevelsRegistry getLevelsRegistry() {
        return levelsRegistry;
    }
}
