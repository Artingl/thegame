package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import dev.artingl.Engine.timer.Timer;

public class SphereColliderComponent extends BaseColliderComponent {

    public float radius;

    private SphereCollisionShape shape;

    public SphereColliderComponent(float radius) {
        this.radius = radius;
        this.shape = new SphereCollisionShape(radius);
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
        return "Sphere Collider";
    }

}
