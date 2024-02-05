package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.bullet.collision.PhysicsCollisionObject;

public interface BodyComponent {

    boolean overlaps(BodyComponent body);
    PhysicsCollisionObject getCollisionObject();


}
