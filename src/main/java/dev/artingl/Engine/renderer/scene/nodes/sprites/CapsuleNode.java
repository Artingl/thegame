package dev.artingl.Engine.renderer.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.CapsuleMesh;
import dev.artingl.Engine.renderer.scene.components.collider.CapsuleColliderComponent;

public class CapsuleNode extends SpriteNode {

    public CapsuleNode() {
        this(1, 2);
    }

    public CapsuleNode(float radius, float height) {
        super(new CapsuleMesh(radius, height));
        this.addComponent(new CapsuleColliderComponent(radius, height));
    }

}
