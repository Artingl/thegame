package dev.artingl.Engine;

import dev.artingl.Engine.debug.Debugger;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.debug.Profiler;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.input.InputKeys;
import dev.artingl.Engine.input.InputListener;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.resources.ResourceManager;
import dev.artingl.Engine.threading.ThreadsManager;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.audio.SoundsManager;
import dev.artingl.Engine.world.scene.SceneManager;
import org.apache.commons.io.FileUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL43C.*;

public class Engine implements TickListener, InputListener {

    private static Engine instance;

    public static Engine getInstance() {
        return instance;
    }
    // ----------------

    private final Logger logger;
    private final Display display;
    private final Renderer renderer;
    private final Options options;
    private final ThreadsManager threadsManager;
    private final Profiler profiler;
    private final SceneManager sceneManager;
    private final Debugger debugger;
    private final Input input;
    private final ResourceManager resourceManager;
    private final Timer timer;
    private final SoundsManager soundsManager;
    private final ConcurrentLinkedDeque<EngineEventListener> engineEvents;
    private final List<String> namespaces;
    private final List<URI> libsFolders;
    private final List<Runnable> glContext;

    private boolean reload;

    public Engine() {
        instance = this;

        this.logger = Logger.create("Engine");

        this.timer = new Timer(128);
        this.input = new Input();
        this.profiler = new Profiler();
        this.threadsManager = new ThreadsManager();
        this.options = new Options(this.logger);
        this.display = new Display(this.logger, this.input, "Engine - Untitled Window", 1400, 900);
        this.renderer = new Renderer(this.logger, this);
        this.resourceManager = new ResourceManager(this, this.logger);
        this.sceneManager = new SceneManager();
        this.debugger = new Debugger();
        this.soundsManager = new SoundsManager(this.logger, this);
        this.namespaces = new ArrayList<>();
        this.engineEvents = new ConcurrentLinkedDeque<>();
        this.libsFolders = new ArrayList<>();
        this.glContext = new ArrayList<>();

        this.addLibsFolder(new File("./natives"));
    }

    /**
     * Add folder path to the list of folders where the engine would look for libraries on initialization
     * */
    public void addLibsFolder(File folder) {
        if (!folder.isDirectory())
            throw new InvalidParameterException("Folder " + folder.getAbsolutePath() + " does not exist!");

        this.libsFolders.add(folder.toURI());
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
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

    /**
     * Register namespace to be used in resources
     *
     * @param namespace Namespace to register
     * */
    public void registerNamespace(String namespace) {
        this.namespaces.add(namespace);
    }

    public Collection<String> getNamespaces() {
        return namespaces;
    }

    public void loadLibs() throws Exception {
        this.logger.log(LogLevel.WARNING, "!!! IF YOU CRASHED AFTER THIS LOG, CHECK PATHS TO LIB FOLDERS !!!");

        // Load libs from all folders
        for (URI folder: this.libsFolders) {
            Path directory = Paths.get(folder);

            // Load all textures
            try (Stream<Path> walk = Files.walk(directory, 3).filter(Files::isRegularFile)) {
                for (Iterator<Path> it = walk.iterator(); it.hasNext();) {
                    Path path = it.next();
                    if (!path.toString().endsWith(".dll"))
                        continue;
                    this.logger.log(LogLevel.INFO, "Loading library: " + path);

                    // Verify checksum before loading
                    String checksum = Utils.getMD5Checksum(path.toString());
                    File checksumFile = new File(path + ".checksum");

                    if (checksumFile.isFile()) {
                        String validChecksum = FileUtils.readFileToString(checksumFile, StandardCharsets.UTF_8);
                        if (!validChecksum.equals(checksum)) {
                            throw new EngineException("Invalid checksum for '" + path + "!' '" + checksum + "' != '" + validChecksum + "'");
                        }
                    }
                    else {
                        this.logger.log(LogLevel.WARNING, "No checksum found for '%s'. Checksum: %s", path.toString(), checksum);
                    }

                    // Load the lib
                    System.load(path.toString());
                }
            }
        }
    }

    public void create() throws Exception {
        this.logger.log(LogLevel.INFO, "Setting up the engine");

        this.loadLibs();

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

        glEnable(GL_DEBUG_OUTPUT);
        glDebugMessageCallback((src, type, id, severity, len, msg, userParam) -> {
            if (type == GL_DEBUG_TYPE_ERROR && severity == GL_DEBUG_SEVERITY_HIGH)
                logger.log(LogLevel.INFO, "OPENGL DEBUG: " + GLDebugMessageCallback.getMessage(len, msg));
        }, 0);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);

        String glVersion = glGetString(GL_VERSION);
        logger.log(LogLevel.INFO, "OpenGL v%s", glVersion);

        // Init the renderer
        this.renderer.create();

        // Initialize initial pipeline
        this.sceneManager.init();

        this.timer.subscribe(this);
        this.input.subscribe(this);

        this.input.init();
        this.resourceManager.init();
        this.soundsManager.init();
        this.debugger.init();

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
        glFrontFace(GL_CW);

        this.timer.enterLoop();
    }

    public void terminate() {
        this.sceneManager.cleanup();
        this.debugger.cleanup();
        this.engineEvents.clear();
        this.resourceManager.cleanup();
        this.input.cleanup();
        this.soundsManager.terminate();
        this.timer.terminate();
        this.renderer.terminate();
        this.threadsManager.terminate();
        this.display.terminate();

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
            for (EngineEventListener subscriber: this.engineEvents)
                subscriber.onReload();
            this.reload = false;
        }

        synchronized (this.glContext) {
            for (Runnable runnable: this.glContext)
                runnable.run();
            this.glContext.clear();
        }

        this.display.poll();
        this.profiler.frame();
        this.display.frame();
        this.renderer.frame();
        this.debugger.frame();
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
     * Schedule code to be run in GL context
     * */
    public void glContext(Runnable runnable) {
        synchronized (this.glContext) {
            this.glContext.add(runnable);
        }
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
    public void subscribeEngineEvents(EngineEventListener handler) {
        if (!this.engineEvents.contains(handler))
            this.engineEvents.add(handler);
    }

    /**
     * Unsubscribe for engine's events
     * */
    public void unsubscribeEngineEvents(EngineEventListener handler) {
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
