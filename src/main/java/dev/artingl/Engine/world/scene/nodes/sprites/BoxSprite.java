package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.BoxMesh;
import org.joml.Vector3f;

public class BoxSprite extends SpriteNode {

    private final Vector3f lengths;

    public BoxSprite(Vector3f lengths) {
        super(new BoxMesh(lengths));
        this.lengths = lengths;
    }

    public Vector3f getLengths() {
        return lengths;
    }
}
