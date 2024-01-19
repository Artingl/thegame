package dev.artingl.Game.scene.node;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.renderer.scene.components.collider.BoxColliderComponent;
import dev.artingl.Engine.renderer.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Models;
import org.joml.Vector3f;

public class ShelterNode extends SpriteNode {

    public ShelterNode() {
        super(new ModelMesh(Models.SHELTER));

        // Floor collider
        BoxColliderComponent floorCollider = new BoxColliderComponent(new Vector3f(44.594f, 1.3f, 69.211f));
        floorCollider.setYOffset(0.7f);

        this.addComponent(floorCollider);
    }

}
