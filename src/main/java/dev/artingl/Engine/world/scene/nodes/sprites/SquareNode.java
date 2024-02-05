package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.SquareMesh;
import dev.artingl.Engine.resources.texture.Texture;

public class SquareNode extends SpriteNode {

    public SquareNode() {
        this(1, 1);
    }

    public SquareNode(float width, float height) {
        this(width, height, Texture.MISSING);
    }

    public SquareNode(float width, float height, Texture texture) {
        super(new SquareMesh(width, height, texture));
    }

}
