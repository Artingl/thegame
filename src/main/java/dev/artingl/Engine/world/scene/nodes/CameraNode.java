package dev.artingl.Engine.world.scene.nodes;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.MathUtils;
import dev.artingl.Engine.world.scene.components.CameraComponent;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;

public class CameraNode extends SceneNode implements Viewport {
    private final CameraComponent camera;

    private float fovDelta = 0;
    private float previousFov = -1;
    private float targetFov = -1;
    private Vector3f projectionRotation;
    private Vector3f projectionOffset;

    public CameraNode() {
        this.projectionRotation = new Vector3f();
        this.projectionOffset = new Vector3f();
        this.camera = new CameraComponent();
        this.addComponent(camera);
    }

    public CameraComponent getCamera() {
        return camera;
    }

    /**
     * Set the rotation of the viewport's projection matrix
     * */
    public void setProjectionRotation(Vector3f vec) {
        this.projectionRotation = new Vector3f(vec);
    }

    /**
     * Set the offset of the viewport's projection matrix
     * */
    public void setProjectionOffset(Vector3f vec) {
        this.projectionOffset = new Vector3f(vec);
    }

    /**
     * Smoothly change camera FOV
     *
     * @param newFov New fov to be set.
     * */
    public void smoothFov(float newFov)
    {
        this.previousFov = getFov();
        this.targetFov = newFov;
        this.fovDelta = 0;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        // Check if we need to make fov transition
        if (this.targetFov != -1 && this.previousFov != -1) {
            CameraComponent camera = getComponent(CameraComponent.class);

            float delta = MathUtils.easeInOutCirc(this.fovDelta);
            camera.fov = (this.previousFov * (1 - delta)) + this.targetFov * delta;
            this.fovDelta += 5 / timer.getTickPerSecond();

            // If the delta has reached 1, we should stop the transition
            if (this.fovDelta > 1) {
                this.fovDelta = 0;
                this.targetFov = -1;
                this.previousFov = -1;
            }
        }
    }

    @Override
    public Vector3f getRotation() {
        return getTransform().rotation;
    }

    @Override
    public Vector3f getPosition() {
        return getTransform().position;
    }

    @Override
    public Vector3f getScale() {
        return getTransform().scale;
    }

    @Override
    public Vector3f getProjectionRotation() {
        return projectionRotation;
    }

    @Override
    public Vector3f getProjectionOffset() {
        return projectionOffset;
    }

    public float getFov() {
        return ((CameraComponent) getComponent(CameraComponent.class)).fov;
    }

    @Override
    public float getAspect() {
        return getWidth() / getHeight();
    }

    @Override
    public float getWidth() {
        return Engine.getInstance().getDisplay().getWidth();
    }

    @Override
    public float getHeight() {
        return Engine.getInstance().getDisplay().getHeight();
    }

    @Override
    public float getNearPlane() {
        return ((CameraComponent) getComponent(CameraComponent.class)).nearPlane;
    }

    @Override
    public float getFarPlane() {
        return ((CameraComponent) getComponent(CameraComponent.class)).farPlane;
    }

    @Override
    public float getSize() {
        return ((CameraComponent) getComponent(CameraComponent.class)).size;
    }

    @Override
    public ViewType getViewType() {
        return ((CameraComponent) getComponent(CameraComponent.class)).viewType;
    }

    @Override
    public RenderType getRenderType() {
        return ((CameraComponent) getComponent(CameraComponent.class)).renderType;
    }

    @Override
    public Color getBackgroundColor() {
        return ((CameraComponent) getComponent(CameraComponent.class)).backgroundColor;
    }

    @Override
    public boolean isShadowMappingEnabled() {
        return ((CameraComponent) getComponent(CameraComponent.class)).enableShadowMapping;
    }

    @Override
    public boolean isPostprocessingEnabled() {
        return ((CameraComponent) getComponent(CameraComponent.class)).enablePostprocessing;
    }

}
