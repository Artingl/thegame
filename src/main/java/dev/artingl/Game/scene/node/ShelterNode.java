package dev.artingl.Game.scene.node;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Models;

public class ShelterNode extends SpriteNode {

    public ShelterNode() {
        super(new ModelMesh(Models.SHELTER));

        // Floor collider
//        BoxColliderComponent floorCollider = new BoxColliderComponent(new Vector3f(44.594f, 1.3f, 69.211f));
//        floorCollider.setOffset(new Vector3f(0, 0.7f, 0));

        // Wall 0 collider
//        BoxColliderComponent wall0Collider = new BoxColliderComponent(new Vector3f(2.12f, 71.5f, 12f));
//        floorCollider.setOffset(new Vector3f(-22, -0.23f, 5.48f));
//
//        RigidBodyComponent rb = new RigidBodyComponent();
//        rb.enableBody = false;
//        rb.enableRotation = false;

//        this.addComponent(floorCollider);
//        this.addComponent(wall0Collider);
//        this.addComponent(rb);
    }

}
