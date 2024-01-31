package dev.artingl.Engine.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.BoxMesh;
import org.joml.Vector3f;

public class BoxNode extends SpriteNode {

    public BoxNode(Vector3f lengths) {
        super(new BoxMesh(lengths));
    }

}
