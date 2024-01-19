package dev.artingl.Engine.renderer.scene.components.collider;

import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.renderer.scene.components.Component;
import dev.artingl.Engine.renderer.scene.components.RigidBodyComponent;
import dev.artingl.Engine.renderer.scene.components.transform.TransformComponent;
import dev.artingl.Engine.timer.Timer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.internal.DxGeom;

public class BaseColliderComponent extends Component {

    public float yOffset;

    protected Runnable triggerHandler;
    protected boolean isColliderBuilt = false;
    protected boolean isGeometryAdded = false;

    @Override
    public void cleanup() {
        super.cleanup();
        DGeom geometry = this.getGeometry();
        if (geometry != null) {
            geometry.destroy();
        }
    }

    /**
     * Build the collider
     */
    protected void buildCollider() {
        getEngine().getLogger().log(LogLevel.UNIMPLEMENTED, "Could not build mesh for base collider.");
    }

    /**
     * Get collider geometry
     */
    public DGeom getGeometry() {
        return null;
    }

    /**
     * Set trigger handler that will be called on collision with any other colliders.
     */
    public void setCollisionHandler(Runnable handler) {
        this.triggerHandler = handler;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        DxGeom geometry = (DxGeom) getGeometry();

        TransformComponent transform = getNode().getTransform();
        RigidBodyComponent rigidBody = getNode().getComponent(RigidBodyComponent.class);

        // Build the collider mesh if we haven't yet
        if (!isColliderBuilt) {
            if (getGeometry() != null)
                this.getNode().getScene().getCollisionHandlers().remove(getGeometry());
            this.buildCollider();
            this.getNode().getScene().getCollisionHandlers().put(getGeometry(), () -> {
                if (this.triggerHandler != null)
                    this.triggerHandler.run();
            });
        } else if (geometry != null) {
            if (!isGeometryAdded) {
                getNode().getScene().getSpace().add(this.getGeometry());
                this.isGeometryAdded = true;
            }

            if (rigidBody != null) {
                geometry.setBody(rigidBody.getBody());
            } else {
                geometry.setPosition(
                        transform.position.x,
                        transform.position.z,
                        transform.position.y + this.yOffset
                );
            }
        }
    }

    @Override
    public String getName() {
        return "Collider";
    }

    /**
     * Set Y offset that would be added when calculating collision with this collider.
     * Note: If you have rigid body, set the Y offset in the rigid body itself
     * */
    public void setYOffset(float offset) {
        this.yOffset = offset;
    }
}
