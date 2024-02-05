package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import dev.artingl.Engine.timer.Timer;

public class CapsuleColliderComponent extends BaseColliderComponent {

    public float radius;
    public float height;

    private CapsuleCollisionShape shape;

    public CapsuleColliderComponent(float radius, float height) {
        this.radius = radius;
        this.height = height;
        this.shape = new CapsuleCollisionShape(radius, height);
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
        return "Capsule Collider";
    }

}
