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
    public void update() {
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
        float nearPlane = currentViewport.getNearPlane();
        float farPlane = currentViewport.getFarPlane();
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
                            nearPlane, farPlane)
            );
        } else {
            float halfw = (width * size) / 2, halfh = (height * size) / 2;
            this.proj.set(
                    new Matrix4f().ortho(-halfw, halfw, -halfh, halfh, nearPlane, farPlane)
            );
        }

        // Rotate the projection
        Vector3f rot = currentViewport.getProjectionRotation();
        this.proj.rotate((float) Math.toRadians(rot.x), 1, 0, 0);
        this.proj.rotate((float) Math.toRadians(rot.y), 0, 1, 0);
        this.proj.rotate((float) Math.toRadians(rot.z), 0, 0, 1);

        this.frustum.set(new Matrix4f(proj).mul(view));
    }

    /**
     * Get viewport's position
     * */
    public Vector3f getPosition() {
        if (currentViewport == null) {
            return new Vector3f();
        }

        return currentViewport.getPosition();
    }

    /**
     * Get viewport's rotation
     * */
    public Vector3f getRotation() {
        if (currentViewport == null) {
            return new Vector3f();
        }

        return currentViewport.getRotation();
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
        program.updateViewport(this);
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

    /**
     * Clear the viewport screen
     * */
    public void clear() {
        if (currentViewport == null)
            return;

        // Clear the screen with our color
        Color color = currentViewport.getBackgroundColor();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(color.red() / 255.0f, color.green() / 255.0f, color.blue() / 255.0f, color.alpha() / 255.0f);
    }
}
