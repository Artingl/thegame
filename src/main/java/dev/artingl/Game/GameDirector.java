package dev.artingl.Game;

import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.renderer.postprocessing.effects.PostprocessBloomEffect;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.world.scene.SceneManager;
import dev.artingl.Game.common.vm.Sedna;
import dev.artingl.Game.registries.LevelsRegistry;
import dev.artingl.Game.render.postprocess.PostprocessPaletteEffect;
import dev.artingl.Game.scene.FurryScene;
import dev.artingl.Game.scene.MapScene;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.scene.MainMenuScene;

public class GameDirector {
    private static GameDirector instance;
    public static GameDirector getInstance() {
        return instance;
    }

    private final Logger logger;
    private final Engine engine;
    private final LevelsRegistry levelsRegistry;

    private boolean isRunning;

    public GameDirector() {
        GameDirector.instance = this;

        Sedna.init();

        /* Initialize engine */
        this.engine = new Engine();
        this.engine.enableDebugger();
        this.engine.registerNamespace("thegame");

        this.logger = this.engine.getLogger();

        /* Initialize game related classes */
        this.levelsRegistry = new LevelsRegistry();

        /* Replace System.err and System.out with our logger */
        System.setOut(this.logger);
        System.setErr(this.logger);

        try {
            /* Initialize the engine.
             * This will create the GL context for current thread,
             * make the window and initialize the renderer with the pipeline
             */
            this.engine.create();

            /* Load out main font */
            this.engine.getRenderer().getFontManager().loadFont(new Resource("thegame", "font/main.ttf"));
        } catch (Exception e) {
            /* We got critical error.
             * Print it out the error and exit with code 1
             */
            logger.exception(e, "Got critical error!");
            System.exit(1);
        }

        this.engine.getDisplay().setVsync(false);

        /* Add post-processing effects */
        this.engine.getRenderer().getPostprocessing().addEffect(new PostprocessBloomEffect());
        this.engine.getRenderer().getPostprocessing().addEffect(new PostprocessPaletteEffect());

        /* Register levels */
        this.levelsRegistry.registerLevel(new Resource("thegame", "level/park"), new Level());
        this.levelsRegistry.switchLevel(new Resource("thegame", "level/park"));

        /* Register scenes */
        SceneManager sceneManager = engine.getSceneManager();

        sceneManager.registerScene(new Resource("thegame", "scene/map"), new MapScene());
        sceneManager.switchScene(new Resource("thegame", "scene/map"));

        sceneManager.registerScene(new Resource("thegame", "scene/furry"), new FurryScene());
//        sceneManager.switchScene(new Resource("thegame", "scene/furry"));

        sceneManager.registerScene(new Resource("thegame", "scene/main_menu"), new MainMenuScene());
//        sceneManager.switchScene(new Resource("thegame", "scene/main_menu"));
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

    public LevelsRegistry getLevelsRegistry() {
        return levelsRegistry;
    }
}
