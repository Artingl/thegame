package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.CapsuleMesh;

public class CapsuleNode extends SpriteNode {

    public CapsuleNode() {
        this(1, 2);
    }

    public CapsuleNode(float radius, float height) {
        super(new CapsuleMesh(radius, height));
    }

}
