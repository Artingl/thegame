package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;

public class BoxColliderComponent extends BaseColliderComponent {

    public Vector3f length;

    private BoxCollisionShape shape;

    public BoxColliderComponent(Vector3f xyzLengths) {
        this.length = xyzLengths;
        this.shape = new BoxCollisionShape(new com.jme3.math.Vector3f(length.x, length.y, length.z));
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected CollisionShape buildCollider() {
        return shape;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
    }

    @Override
    public String getName() {
        return "Box Collider";
    }

}
