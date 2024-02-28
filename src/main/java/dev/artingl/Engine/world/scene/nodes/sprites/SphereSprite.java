package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.resources.texture.Texture;

public class SphereSprite extends SpriteNode {

    public SphereSprite() {
        this(4);
    }

    public SphereSprite(float radius) {
        this(radius, Texture.MISSING);
    }

    public SphereSprite(float radius, Texture texture) {
        super(new SphereMesh(Color.WHITE, texture, radius));
    }

}
