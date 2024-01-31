package dev.artingl.Engine.scene.components.collider;

import dev.artingl.Engine.timer.Timer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxSphere;

public class SphereColliderComponent extends BaseColliderComponent {

    public float radius;

    private DxSphere sphereGeometry;

    public SphereColliderComponent(float radius) {
        this.radius = radius;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.sphereGeometry.destroy();
        this.sphereGeometry = null;
    }

    @Override
    public DGeom getGeometry() {
        return sphereGeometry;
    }

    @Override
    protected void buildCollider() {
        this.sphereGeometry = (DxSphere) OdeHelper.createSphere(null, radius);
        this.isColliderBuilt = true;
    }

    @Override
    public void tick(Timer timer) {
        if (this.sphereGeometry != null) {
            this.sphereGeometry.setRadius(radius);
        }

        super.tick(timer);
    }

    @Override
    public String getName() {
        return "Sphere Collider";
    }

}
