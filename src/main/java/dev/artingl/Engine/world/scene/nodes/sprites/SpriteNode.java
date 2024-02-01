package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;

public class SpriteNode extends SceneNode {

    private final IMesh mesh;

    public SpriteNode(IMesh mesh) {
        this.mesh = mesh;
        this.addComponent(new MeshComponent(mesh));
    }

    public IMesh getMesh() {
        return mesh;
    }
}
