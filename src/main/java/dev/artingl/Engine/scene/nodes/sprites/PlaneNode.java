package dev.artingl.Engine.scene.nodes.sprites;

import dev.artingl.Engine.renderer.mesh.PlaneMesh;
import dev.artingl.Engine.scene.components.collider.MeshColliderComponent;

public class PlaneNode extends SpriteNode {

    public PlaneNode() {
        this(16, 16);
    }

    public PlaneNode(float width, float height) {
        super(new PlaneMesh(width, height));

        MeshColliderComponent collider = new MeshColliderComponent();
        this.addComponent(collider);
    }

}
