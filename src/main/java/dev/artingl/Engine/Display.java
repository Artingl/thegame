package dev.artingl.Engine;

import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Display {

    private final Logger logger;
    private final Input input;

    private long windowId;

    private String title;
    private int lastWidth, lastHeight;
    private int lastX, lastY;
    private int width, height;
    private int x, y;
    private boolean isVsyncEnabled = false;
    private boolean isFullscreenEnabled = false;
    private boolean isInFocus = true;
    private boolean isCursorCaptured = false;
    private boolean cursorWasCaptured = false;

    private Vector2f mousePosition = new Vector2f();
    private Vector2f lastMousePosition = new Vector2f();
    private Vector2f mouseDelta = new Vector2f();

    /**
     * @param logger Engine logger
     * @param title  Title of the window that would be created
     * @param width  Window's width
     * @param height Window's height
     */
    public Display(Logger logger, Input input, String title, int width, int height) {
        this.logger = logger;
        this.input = input;

        this.title = title;
        this.width = width;
        this.height = height;
    }

    /**
     * Will set up the GL context for current thread, setup callbacks and create the window
     */
    public void create() {
        // Configure GLFW window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // TODO: apple only??
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        this.windowId = glfwCreateWindow(width, height, title, 0, 0);
        if (this.windowId == 0)
            throw new IllegalStateException("Unable to create window");

        // Setup all necessary callbacks for the window
        this.setupCallbacks();

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    windowId,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId);

        // Make the window visible
        glfwShowWindow(windowId);

        glfwFocusWindow(windowId);
    }

    /**
     * Terminate everything (GL context, window, etc.)
     */
    public void terminate() {
        // Terminate the window and callbacks
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);
    }

    private void setupCallbacks() {
        // Resize callback and move
        glfwSetFramebufferSizeCallback(windowId, (window, width, height) -> {
            this.width = width;
            this.height = height;
        });

        glfwSetWindowPosCallback(windowId, (window, xpos, ypos) -> {
            this.x = xpos;
            this.y = ypos;
        });

        // Keyboard callback
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            this.input.setKeyboardStateArray(key, action);
            this.input.setKeyboardModState(mods);
        });

        // Mouse callbacks
        glfwSetMouseButtonCallback(windowId, (window, button, action, mods) -> {
            this.input.setMouseStateArray(button, action);
        });

        glfwSetScrollCallback(windowId, (window, xoffset, yoffset) -> {
            this.input.setMouseWheel((int) xoffset, (int) yoffset);
        });

        glfwSetCursorPosCallback(windowId, (window, xpos, ypos) -> {
            this.mousePosition = new Vector2f((float) xpos, (float) ypos);
        });

        // Other window callbacks
        glfwSetWindowFocusCallback(windowId, (window, focused) -> {
            this.isInFocus = focused;
        });
    }

    public void setVsync(boolean state) {
        this.isVsyncEnabled = state;
        glfwSwapInterval(state ? 1 : 0);
    }

    public boolean isVsyncEnabled() {
        return isVsyncEnabled;
    }

    public boolean isAlive() {
        return !glfwWindowShouldClose(windowId);
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWindowX() {
        return x;
    }

    public int getWindowY() {
        return y;
    }

    public void setWidth(int width) {
        glfwSetWindowSize(windowId, width, height);
    }

    public void setHeight(int height) {
        glfwSetWindowSize(windowId, width, height);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(windowId, width, height);
    }

    public Input getInput() {
        return input;
    }

    public void setTitle(String title) {
        glfwSetWindowTitle(windowId, title);
        this.title = title;
    }

    public void frame() {
        glfwSwapBuffers(windowId);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Hide/show cursor based on capture status
        glfwSetInputMode(windowId, GLFW_CURSOR, this.isCursorCaptured ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    public void poll() {
        glfwPollEvents();
    }

    public void setFullscreen(boolean state) {
        this.isFullscreenEnabled = state;

        if (state) {
            this.lastWidth = this.width;
            this.lastHeight = this.height;
            this.lastX = this.x;
            this.lastY = this.y;

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowMonitor(windowId, glfwGetPrimaryMonitor(), 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
        }
        else {
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowMonitor(windowId, 0, this.lastX, this.lastY, this.lastWidth, this.lastHeight, vidmode.refreshRate());
        }
    }

    public boolean isFullscreenEnabled() {
        return isFullscreenEnabled;
    }

    public void tick(Timer timer) {
        this.input.setMousePosition(this.mousePosition.x, this.mousePosition.y);
        if (this.isCursorCaptured && isInFocus()) {
            if (!this.cursorWasCaptured) {
                this.lastMousePosition = this.mousePosition;
                this.cursorWasCaptured = true;
            }

            this.mouseDelta = new Vector2f(this.mousePosition).sub(this.lastMousePosition);
            this.lastMousePosition = this.mousePosition;
        }
        else {
            this.cursorWasCaptured = false;
            this.mouseDelta = new Vector2f(0, 0);
        }
    }

    public void clear(Color color) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(color.red() / 255.0f, color.green() / 255.0f, color.blue() / 255.0f, color.alpha() / 255.0f);
    }

    public float getAspectRatio() {
        return (float)width / (float)height;
    }

    public long getWindowId() {
        return windowId;
    }

    public Vector2f getMouseDelta() {
        return mouseDelta;
    }

    public Vector2f getMousePosition() {
        return mousePosition;
    }

    public boolean isInFocus() {
        return isInFocus;
    }

    public boolean captureCursor(boolean state) {
        if (isInFocus()) {
            this.isCursorCaptured = state;
            return state;
        }

        return false;
    }
}
