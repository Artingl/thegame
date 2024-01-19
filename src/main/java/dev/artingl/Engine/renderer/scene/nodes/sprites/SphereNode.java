package dev.artingl.Engine.renderer.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.renderer.scene.components.collider.SphereColliderComponent;

public class SphereNode extends SpriteNode {

    public SphereNode() {
        this(4);
    }

    public SphereNode(float radius) {
        super(new SphereMesh(radius));
        this.addComponent(new SphereColliderComponent(radius));
    }

}
