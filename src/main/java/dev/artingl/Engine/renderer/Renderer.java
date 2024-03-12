package dev.artingl.Engine.renderer;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.debug.Profiler;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.MeshManager;
import dev.artingl.Engine.renderer.visual.FontManager;
import dev.artingl.Engine.renderer.visual.postprocessing.PostprocessManager;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.viewport.ViewportManager;
import dev.artingl.Engine.renderer.visual.shadow.ShadowsManager;
import dev.artingl.Engine.world.scene.BaseScene;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31C.glDrawElementsInstanced;

public class Renderer {

    private final Logger logger;
    private final Engine engine;

    private final ViewportManager viewport;
    private final PostprocessManager postprocessManager;
    private final ShadowsManager shadowsManager;
    private final MeshManager meshManager;
    private final FontManager fontManager;
    private ShaderProgram programInUse;
    private int vaoInUse;
    private int eboInUse;
    private Framebuffer currentFramebuffer;
    private boolean isWireframeEnabled;
    private final Framebuffer mainFramebuffer, uiFramebuffer;

    public Renderer(Logger logger, Engine engine) {
        this.logger = logger;
        this.engine = engine;
        this.viewport = new ViewportManager(this.logger, this);
        this.shadowsManager = new ShadowsManager(this.logger, this);
        this.postprocessManager = new PostprocessManager(this.logger);
        this.meshManager = new MeshManager(this.logger, this);
        this.fontManager = new FontManager(this.logger, this);
        this.isWireframeEnabled = false;
        this.uiFramebuffer = new Framebuffer();
        this.mainFramebuffer = new Framebuffer();
//        this.pipeline.append(this.shadowsManager);
//        this.pipeline.append(this.postprocessManager);
    }

    public void create() throws EngineException {
        this.fontManager.init();
        this.uiFramebuffer.init();
        this.mainFramebuffer.init();
        this.meshManager.init();
        this.shadowsManager.init();
        this.postprocessManager.init();
    }

    public void terminate() {
        this.fontManager.cleanup();
        this.uiFramebuffer.cleanup();
        this.mainFramebuffer.cleanup();
        this.shadowsManager.cleanup();
        this.postprocessManager.cleanup();
        this.meshManager.cleanup();
    }

    /**
     * Make all calls necessary for the frame to be rendered
     */
    public void frame() throws EngineException {
        BaseScene scene = engine.getSceneManager().getCurrentScene();
        if (scene == null || this.mainFramebuffer.updateBuffer() || this.uiFramebuffer.updateBuffer())
            return;

        this.programInUse = null;
        this.currentFramebuffer = null;
        this.vaoInUse = -1;
        this.eboInUse = -1;

        this.fontManager.frame();

        if (isWireframeEnabled)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        else
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        this.uiFramebuffer.clear(this, Color.TRANSPARENT);
        this.mainFramebuffer.clear(this, viewport.getBackgroundColor());
        bindFramebuffer(mainFramebuffer);
        scene.prepareFrame(this);
        bindFramebuffer(mainFramebuffer);
        scene.render(this, BaseScene.Layer.MAIN, scene.getMainCamera());
        this.postprocessManager.render(this);
        bindFramebuffer(null);
        scene.render(this, BaseScene.Layer.UI, scene.getUiCamera());
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        bindFramebuffer(null);
    }

    public ViewportManager getViewport() {
        return this.viewport;
    }

    public void useShader(ShaderProgram program) {
        if (program == null) {
            GL20C.glUseProgram(0);
            this.programInUse = null;
            return;
        }

        if (!program.equals(this.programInUse)) {
            GL20C.glUseProgram(program.getProgramId());
            this.programInUse = program;
        }
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    @Nullable
    public ShaderProgram getCurrentProgram() {
        return programInUse;
    }

    public void bindFramebuffer(Framebuffer fb) {
        if (fb == null) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            return;
        }

        Display display = Engine.getInstance().getDisplay();
        this.currentFramebuffer = fb;
        glBindFramebuffer(GL_FRAMEBUFFER, fb.getBufferId());
        glViewport(0, 0, display.getWidth(), display.getHeight());
        Engine.getInstance().getProfiler().incCounter(Profiler.Task.FRAMEBUFFER_BINDS);
    }

    public Framebuffer getCurrentFramebuffer() {
        return currentFramebuffer;
    }

    public Framebuffer getMainFramebuffer() {
        return mainFramebuffer;
    }

    public Framebuffer getUiFramebuffer() {
        return uiFramebuffer;
    }

    public void drawCall(DrawCall type, int array, int mode, int count) {
        long start = System.nanoTime();
        if (array <= 0 || mode <= 0 || count <= 0)
            return;

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
        Engine.getInstance().getProfiler().addGpuTime((System.nanoTime() - start) / 1000000f);
    }

    public void drawCallInstanced(DrawCall type, int array, int mode, int count, int n) {
        long start = System.nanoTime();
        if (array <= 0 || mode <= 0 || count <= 0 || n <= 0)
            return;

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
        Engine.getInstance().getProfiler().addGpuTime((System.nanoTime() - start) / 1000000f);
    }

    public MeshManager getMeshManager() {
        return meshManager;
    }

    /**
     * Updates wireframe start
     *
     * @param state New state to be set
     */
    public void setWireframe(boolean state) {
        this.isWireframeEnabled = state;
    }

    /**
     * Tells is wireframe currently enabled
     */
    public boolean isWireframeEnabled() {
        return isWireframeEnabled;
    }

    /**
     * Get post-process controller
     */
    public PostprocessManager getPostprocessing() {
        return this.postprocessManager;
    }

    public enum DrawCall {
        ARRAYS, ELEMENTS
    }
}
