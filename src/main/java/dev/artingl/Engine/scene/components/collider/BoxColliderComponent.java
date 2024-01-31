package dev.artingl.Engine.scene.components.collider;

import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxBox;

public class BoxColliderComponent extends BaseColliderComponent {

    public Vector3f length;

    private DxBox boxGeometry;

    public BoxColliderComponent(Vector3f xyzLengths) {
        this.length = xyzLengths;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.boxGeometry.destroy();
        this.boxGeometry = null;
    }

    @Override
    public DGeom getGeometry() {
        return boxGeometry;
    }

    @Override
    protected void buildCollider() {
        this.boxGeometry = (DxBox) OdeHelper.createBox(null, length.x, length.z, length.y);
        this.isColliderBuilt = true;
    }

    @Override
    public void tick(Timer timer) {
        if (this.boxGeometry != null)
            this.boxGeometry.setLengths(length.x, length.z, length.y);
        super.tick(timer);
    }

    @Override
    public String getName() {
        return "Box Collider";
    }

}
