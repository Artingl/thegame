package dev.artingl.Engine.renderer;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.debug.Profiler;
import dev.artingl.Engine.renderer.mesh.MeshManager;
import dev.artingl.Engine.renderer.pipeline.IPipeline;
import dev.artingl.Engine.renderer.pipeline.PipelineManager;
import dev.artingl.Engine.renderer.postprocessing.Postprocessing;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Options;
import org.lwjgl.opengl.GL20C;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class Renderer {

    private final Logger logger;

    private final PipelineManager pipeline;
    private final Viewport viewport;
    private final Postprocessing postprocessing;
    private final MeshManager meshManager;
    private ShaderProgram programInUse;
    private int vaoInUse;
    private int eboInUse;
    private int currentFramebuffer;
    private boolean isWireframeEnabled;

    private int renderTexture, framebuffer, depthBuffer;

    public Renderer(Logger logger) {
        this.logger = logger;
        this.viewport = new Viewport(this.logger, this);
        this.pipeline = new PipelineManager(this.logger, this);
        this.postprocessing = new Postprocessing(this.logger);
        this.meshManager = new MeshManager(this.logger, this);
        this.isWireframeEnabled = false;

//        this.pipeline.append(this.postprocessing);
    }

    public void create() throws EngineException {
        this.pipeline.init();

        Display display = Engine.getInstance().getDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Initialize render texture and depth buffer
        this.framebuffer = glGenFramebuffers();
        this.renderTexture = glGenTextures();
        this.depthBuffer = glGenBuffers();

        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        glBindTexture(GL_TEXTURE_2D, this.renderTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        glBindBuffer(GL_RENDERBUFFER, this.depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.depthBuffer);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.renderTexture, 0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        // Check framebuffer
        int status;
        if ((status = glCheckFramebufferStatus(GL_FRAMEBUFFER)) != GL_FRAMEBUFFER_COMPLETE)
            throw new EngineException("Bad framebuffer status: " + status);

        this.meshManager.init();
    }

    public void terminate() {
        glDeleteBuffers(this.depthBuffer);
        glDeleteTextures(this.renderTexture);
        glDeleteFramebuffers(this.framebuffer);
        this.pipeline.cleanup();
        this.meshManager.cleanup();
    }

    /**
     * Add an instance to the pipeline
     *
     * @param instance The instance to be added
     */
    public void pipelineAdd(IPipeline instance) {
        this.logger.log(LogLevel.INFO, "Appending new interface to the pipeline: %s", instance.toString());
        this.pipeline.append(instance);

        // Move postprocessing instance to the end of the pipeline
        this.pipeline.makeLast(this.postprocessing);

        // If debugger is enabled move it to the end of the pipeline so it will render on top of everything
        // TODO: BAD
        Engine engine = Engine.getInstance();
        if (engine.getOptions().getBoolean(Options.Values.DEBUG))
            this.pipeline.makeLast(engine.getDebugger());
    }

    /**
     * Make all calls necessary for the frame to be rendered (make pipeline calls, etc.)
     */
    public void frame() throws EngineException {
        this.programInUse = null;
        this.vaoInUse = -1;
        this.eboInUse = -1;

        bindFramebuffer(framebuffer);
        this.viewport.update();
        this.pipeline.call();
        glUseProgram(0);
        bindFramebuffer(0);
    }

    public Viewport getViewport() {
        return this.viewport;
    }

    public void useShader(ShaderProgram program) {
        if (!program.equals(this.programInUse)) {
            GL20C.glUseProgram(program.getProgramId());
            this.programInUse = program;
        }
    }

    public ShaderProgram getCurrentProgram() {
        return programInUse;
    }

    public void bindFramebuffer(int id) {
        if (this.currentFramebuffer == id)
            return;

        Display display = Engine.getInstance().getDisplay();
        this.currentFramebuffer = id;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);//id);
        glViewport(0, 0, display.getWidth(), display.getHeight());

        Engine.getInstance().getProfiler().incCounter(Profiler.Task.FRAMEBUFFER_BINDS);
    }

    public int getCurrentFramebuffer() {
        return currentFramebuffer;
    }

    public void drawCall(DrawCall type, int array, int mode, int count) {
        switch (type) {
            case ARRAYS -> {
                if (array != this.vaoInUse) {
                    glBindVertexArray(array);
                    this.vaoInUse = array;
                    this.eboInUse = -1;
                }
                glDrawArrays(mode, 0, count);
            }

            case ELEMENTS -> {
                if (array != this.vaoInUse) {
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, array);
                    this.vaoInUse = array;
                    this.eboInUse = -1;
                }
                glDrawElements(mode, count, GL_UNSIGNED_INT, 0);
            }
        }

        Engine.getInstance().getProfiler().incCounter(Profiler.Task.DRAW_CALLS);
        Engine.getInstance().getProfiler().addCounter(Profiler.Task.VERTICES_DRAWN, count);
    }

    public void drawCallInstanced(DrawCall type, int array, int mode, int count, int n) {
        switch (type) {
            case ARRAYS -> {
                if (array != this.vaoInUse) {
                    glBindVertexArray(array);
                    this.vaoInUse = array;
                    this.eboInUse = -1;
                }
                glDrawArraysInstanced(mode, 0, count, n);
            }

            case ELEMENTS -> {
                if (array != this.vaoInUse) {
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, array);
                    this.vaoInUse = array;
                    this.eboInUse = -1;
                }
                glDrawElementsInstanced(mode, count, GL_UNSIGNED_INT, 0, n);
            }
        }

        Engine.getInstance().getProfiler().incCounter(Profiler.Task.DRAW_CALLS);
        Engine.getInstance().getProfiler().addCounter(Profiler.Task.VERTICES_DRAWN, count);
    }

    public int getRenderTextureId() {
        return renderTexture;
    }

    public int getDepthRenderBuffer() {
        return depthBuffer;
    }

    public int getFramebufferId() {
        return framebuffer;
    }

    public boolean isPostprocessingEnabled() {
        return this.viewport.isPostprocessingEnabled();
    }

    public PipelineManager getPipeline() {
        return pipeline;
    }

    public MeshManager getMeshManager() {
        return meshManager;
    }

    /**
     * Updates wireframe start
     *
     * @param state New state to be set
     * */
    public void setWireframe(boolean state) {
        this.isWireframeEnabled = state;
    }

    /**
     * Tells is wireframe currently enabled
     * */
    public boolean isWireframeEnabled() {
        return isWireframeEnabled;
    }

    /**
     * Get post-process controller
     * */
    public Postprocessing getPostprocessing() {
        return this.postprocessing;
    }

    public enum DrawCall {
        ARRAYS, ELEMENTS
    }
}
