package dev.artingl.Engine;

import dev.artingl.Engine.audio.SoundsManager;
import dev.artingl.Engine.debug.Debugger;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.debug.Profiler;
import dev.artingl.Engine.input.IInput;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.input.InputKeys;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.texture.TextureManager;
import dev.artingl.Engine.threading.ThreadsManager;
import dev.artingl.Engine.renderer.scene.SceneManager;
import dev.artingl.Engine.timer.ITick;
import dev.artingl.Engine.timer.Timer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.ode4j.ode.OdeHelper;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;

public class Engine implements ITick, IInput {

    private static Engine instance;

    public static Engine getInstance() {
        return instance;
    }
    // ----------------

    private final String mainNamespace;

    private final Logger logger;
    private final Display display;
    private final Renderer renderer;
    private final Options options;
    private final ThreadsManager threadsManager;
    private final Profiler profiler;
    private final SceneManager sceneManager;
    private final Debugger debugger;
    private final Input input;
    private final TextureManager textureManager;
    private final Timer timer;
    private final SoundsManager soundsManager;
    private final ConcurrentLinkedDeque<IEngineEvent> engineEvents;

    private boolean reload;

    public Engine(String name) {
        instance = this;

        this.mainNamespace = name;
        this.logger = Logger.create(name);

        this.timer = new Timer(128);
        this.input = new Input();
        this.profiler = new Profiler();
        this.threadsManager = new ThreadsManager();
        this.options = new Options(this.logger);
        this.display = new Display(this.logger, this.input, name, 1400, 900);
        this.renderer = new Renderer(this.logger);
        this.textureManager = new TextureManager(this.logger);
        this.sceneManager = new SceneManager();
        this.debugger = new Debugger();
        this.soundsManager = new SoundsManager(this.logger, this);
        this.engineEvents = new ConcurrentLinkedDeque<>();
    }

    public Input getInput() {
        return input;
    }

    public Timer getTimer() {
        return timer;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public Profiler getProfiler() {
        return profiler;
    }

    public ThreadsManager getThreadsManager() {
        return threadsManager;
    }

    public SoundsManager getSoundsManager() {
        return soundsManager;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public Options getOptions() {
        return options;
    }

    public Logger getLogger() {
        return logger;
    }

    public Display getDisplay() {
        return display;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void create() throws Exception {
        this.logger.log(LogLevel.INFO, "Setting up the engine");

        // Setup error callback for GLFW
        GLFWErrorCallback.createPrint(this.logger.getErrStream()).set();

        // Initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Initialize the display and renderer
        this.display.create();
        this.display.setVsync(true);

        // Initialize OpenGL because after creating display we have GL context in this thread
        GL.createCapabilities();

        String glVersion = glGetString(GL_VERSION);
        logger.log(LogLevel.INFO, "OpenGL v%s", glVersion);

        // Load textures
        this.textureManager.load(new Resource(this.mainNamespace, "textures"));

        // Init the renderer
        this.renderer.create();

        // Initialize initial pipeline
        this.renderer.pipelineAdd(this.sceneManager);

        this.timer.subscribe(this);
        this.input.subscribe(this);

        this.textureManager.init();
        this.soundsManager.init();

        // Setup OpenGL
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glClearDepth(1.0D);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        this.timer.enterLoop();

        // Initialize ode4j
        OdeHelper.initODE2(0);
    }

    public void terminate() {
        this.engineEvents.remove();
        this.textureManager.cleanup();
        this.soundsManager.terminate();
        this.timer.terminate();
        this.renderer.terminate();
        this.display.terminate();
        OdeHelper.closeODE();

        // Terminate glfw
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void panic(String message) {
        this.logger.log(LogLevel.ERROR, "Panic: " + message);
        System.exit(1);
    }

    public boolean isAlive() {
        return display.isAlive();
    }

    /**
     * Renders one frame.
     */
    public void frame() throws Exception {
        if (this.reload) {
            this.logger.log(LogLevel.INFO, "Reloading!");

            // Tell all engine subscribers that they need to reload
            for (IEngineEvent subscriber: this.engineEvents)
                subscriber.onReload();
            this.reload = false;
        }

        this.display.poll();
        this.profiler.frame();
        this.input.frame();
        this.display.frame();
        this.renderer.frame();
        this.soundsManager.frame();
    }

    public String getGraphicsInfo() {
        return glGetString(GL_RENDERER);
    }

    @Override
    public void tick(Timer timer) {
        this.display.tick(timer);
    }

    /**
     * Reload all resources, shaders, etc.
     * */
    public void reload() {
        // Flip the reload variable, so it can be called in the main thread
        this.reload = true;
    }

    /**
     * Subscribe for engine's events
     * */
    public void subscribeEngineEvents(IEngineEvent handler) {
        if (!this.engineEvents.contains(handler))
            this.engineEvents.add(handler);
    }

    public void unsubscribeEngineEvents(IEngineEvent handler) {
        this.engineEvents.remove(handler);
    }

    /**
     * Enables rendering of debug windows
     * */
    public void enableDebugger() {
        getOptions().set(Options.Values.DEBUG, true);
    }

    @Override
    public void keyboardEvent(Input input, Input.State state, int key) {
        // Toggle fullscreen mode in F11
        if (key == InputKeys.KEY_F11 && state.isPressed())
            getDisplay().setFullscreen(!getDisplay().isFullscreenEnabled());
    }

    @Override
    public void mouseButtonEvent(Input input, Input.State state, int key) {

    }

    @Override
    public void mouseWheelEvent(Input input, int wheelX, int wheelY) {

    }

    @Override
    public void mouseMoveEvent(Input input, float x, float y) {

    }
}
