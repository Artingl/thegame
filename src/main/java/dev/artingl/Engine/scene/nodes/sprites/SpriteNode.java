package dev.artingl.Engine.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.scene.components.MeshComponent;
import dev.artingl.Engine.scene.nodes.SceneNode;

public class SpriteNode extends SceneNode {

    public SpriteNode(IMesh mesh) {
        this.addComponent(new MeshComponent(mesh));
    }

}
