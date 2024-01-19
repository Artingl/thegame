package dev.artingl.Engine.renderer.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.scene.components.MeshComponent;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;

public class SpriteNode extends SceneNode {

    public SpriteNode(IMesh mesh) {
        this.addComponent(new MeshComponent(mesh));
    }

}
