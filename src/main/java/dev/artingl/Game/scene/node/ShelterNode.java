package dev.artingl.Game.scene.node;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Models;

public class ShelterNode extends SpriteNode {

    public ShelterNode() {
        super(new ModelMesh(Models.SHELTER));
//        this.addComponent(new MeshColliderComponent(getMesh()));
    }

}
