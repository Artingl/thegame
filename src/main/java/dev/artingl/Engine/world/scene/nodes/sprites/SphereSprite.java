package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.resources.texture.Texture;

public class SphereSprite extends SpriteNode {

    public SphereSprite() {
        this(4);
    }

    public SphereSprite(float radius) {
        this(radius, Color.WHITE, Texture.MISSING);
    }

    public SphereSprite(float radius, Color color) {
        this(radius, color, Texture.MISSING);
    }

    public SphereSprite(float radius, Texture texture) {
        this(radius, Color.WHITE, texture);
    }

    public SphereSprite(float radius, Color color, Texture texture) {
        super(new SphereMesh(color, texture, radius));
    }

}
