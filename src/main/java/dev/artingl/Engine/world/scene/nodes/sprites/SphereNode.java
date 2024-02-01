package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.SphereMesh;

public class SphereNode extends SpriteNode {

    public SphereNode() {
        this(4);
    }

    public SphereNode(float radius) {
        super(new SphereMesh(radius));
    }

}
