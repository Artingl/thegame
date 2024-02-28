package dev.artingl.Engine.world.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.PlaneMesh;
import dev.artingl.Engine.renderer.mesh.SquareMesh;
import dev.artingl.Engine.resources.texture.Texture;

public class PlaneSprite extends SpriteNode {

    public PlaneSprite() {
        this(1, 1);
    }

    public PlaneSprite(float width, float height) {
        this(width, height, Texture.MISSING);
    }

    public PlaneSprite(float width, float height, Texture texture) {
        super(new PlaneMesh(width, height, texture));
    }

}
