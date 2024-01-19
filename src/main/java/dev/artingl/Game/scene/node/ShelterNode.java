package dev.artingl.Game.scene.node;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.renderer.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Models;

public class ShelterNode extends SpriteNode {

    public ShelterNode() {
        super(new ModelMesh(Models.SHELTER));
    }

}
