package dev.artingl.Engine.renderer.viewport;

import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class Viewport {
    private final Logger logger;
    private final RenderContext renderContext;
    private final Matrix4f view, proj;
    private final FrustumIntersection frustum;

    private IViewport currentViewport;

    public Viewport(Logger logger, Renderer renderer) {
        this.frustum = new FrustumIntersection();
        this.logger = logger;
        this.renderContext = new RenderContext(logger, renderer);

        this.view = new Matrix4f();
        this.proj = new Matrix4f();
    }

    /**
     * Get frustum of the viewport.
     * */
    public FrustumIntersection getFrustum() {
        return frustum;
    }

    /**
     * Set current viewport
     */
    public void setViewport(IViewport viewport) {
        this.currentViewport = viewport;
    }

    /**
     * Get current viewport
     */
    @Nullable
    public IViewport getCurrentViewport() {
        return currentViewport;
    }

    /**
     * Will update all matrices
     */
    public void call() {
        if (currentViewport == null) {
            // No viewport is being used
            this.view.set(new Matrix4f().zero());
            this.proj.set(new Matrix4f().zero());
            return;
        }

        Vector3f position = currentViewport.getPosition();
        Vector3f rotation = currentViewport.getRotation();
        Vector3f scale = currentViewport.getScale();
        float fov = currentViewport.getFov();
        float aspect = currentViewport.getAspect();
        float width = currentViewport.getWidth();
        float height = currentViewport.getHeight();
        float zNear = currentViewport.getZNear();
        float zFar = currentViewport.getZFar();
        float size = currentViewport.getSize();

        // Update view matrix based on the values from current viewport
        this.view.set(new Matrix4f()
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale)
                .translate(new Vector3f(-1).mul(position)));

        // Update proj matrix
        if (currentViewport.getType() == IViewport.Type.PERSPECTIVE) {
            this.proj.set(
                    new Matrix4f().perspective(
                            (float) Math.toRadians(fov), aspect,
                            zNear, zFar)
            );
        } else {
            float halfw = (width * size) / 2, halfh = (height * size) / 2;
            this.proj.set(
                    new Matrix4f().ortho(-halfw, halfw, -halfh, halfh, zNear, zFar)
            );
        }

        // Clear the screen with our color
        Color color = currentViewport.getBackgroundColor();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(color.red() / 255.0f, color.green() / 255.0f, color.blue() / 255.0f, color.alpha() / 255.0f);

        this.frustum.set(new Matrix4f(proj).mul(view));
    }

    /**
     * Returns current background color of the viewport
     * */
    public Color getBackgroundColor() {
        if (currentViewport == null)
            return Color.BLACK;

        return currentViewport.getBackgroundColor();
    }

    /**
     * Uploads matrices to the shader program
     *
     * @param program The target shader program
     */
    public void uploadMatrices(ShaderProgram program) {
        program.updatePVMatrix(proj, view);
    }

    /**
     * Get current projection matrix.
     * */
    public Matrix4f getProjection() {
        return new Matrix4f(proj);
    }

    /**
     * Get current view matrix.
     * */
    public Matrix4f getView() {
        return new Matrix4f(view);
    }

    /**
     * Tells whether to use the post-processing with the viewport
     * */
    public boolean isPostprocessingEnabled() {
        if (currentViewport == null)
            return false;

        return currentViewport.usePostprocessing();
    }
}
