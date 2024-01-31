package dev.artingl.Engine.scene.components.collider;

import dev.artingl.Engine.timer.Timer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxCapsule;

public class CapsuleColliderComponent extends BaseColliderComponent {

    public float radius;
    public float height;

    private DxCapsule capsuleGeometry;

    public CapsuleColliderComponent(float radius, float height) {
        this.radius = radius;
        this.height = height;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.capsuleGeometry.destroy();
        this.capsuleGeometry = null;
    }

    @Override
    public DGeom getGeometry() {
        return capsuleGeometry;
    }

    @Override
    protected void buildCollider() {
        this.capsuleGeometry = (DxCapsule) OdeHelper.createCapsule(radius, height);
        this.isColliderBuilt = true;
    }

    @Override
    public void tick(Timer timer) {
        if (this.capsuleGeometry != null) {
            this.capsuleGeometry.setParams(radius, height);
        }

        super.tick(timer);
    }

    @Override
    public String getName() {
        return "Capsule Collider";
    }

}
