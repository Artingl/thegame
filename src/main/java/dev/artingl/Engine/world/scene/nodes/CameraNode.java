package dev.artingl.Engine.world.scene.nodes;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.MathUtils;
import dev.artingl.Engine.world.scene.components.CameraComponent;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;

public class CameraNode extends SceneNode implements IViewport {

    private float fovDelta = 0;
    private float previousFov = -1;
    private float targetFov = -1;
    private Vector3f projectionRotation;

    public CameraNode() {
        this.projectionRotation = new Vector3f();
        this.addComponent(new CameraComponent());
    }

    /**
     * Set the rotation of the viewport's projection matrix
     * */
    public void setProjectionRotation(Vector3f vec) {
        this.projectionRotation = new Vector3f(vec);
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
    public float getZNear() {
        return ((CameraComponent) getComponent(CameraComponent.class)).zNear;
    }

    @Override
    public float getZFar() {
        return ((CameraComponent) getComponent(CameraComponent.class)).zFar;
    }

    @Override
    public float getSize() {
        return ((CameraComponent) getComponent(CameraComponent.class)).size;
    }

    @Override
    public IViewport.Type getType() {
        return ((CameraComponent) getComponent(CameraComponent.class)).type;
    }

    @Override
    public Color getBackgroundColor() {
        return ((CameraComponent) getComponent(CameraComponent.class)).backgroundColor;
    }

    @Override
    public boolean usePostprocessing() {
        return ((CameraComponent) getComponent(CameraComponent.class)).postprocessing;
    }

}
