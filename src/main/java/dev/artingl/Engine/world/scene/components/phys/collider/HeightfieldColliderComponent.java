package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;

public class HeightfieldColliderComponent extends BaseColliderComponent {

    private final int width;
    private final int depth;
    private final IHeightfieldHandler heightProvider;
    private HeightfieldCollisionShape shape;

    public HeightfieldColliderComponent(int width, int depth, IHeightfieldHandler heightProvider) {
        this.width = width;
        this.depth = depth;
        this.heightProvider = heightProvider;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected CollisionShape buildCollider() {
        if (shape == null) {
            float[] heights = new float[width*depth];
            for (int x = 0; x < width; x++)
                for (int z = 0; z < depth; z++) {
                    heights[z * width + x] = heightProvider.run(x, z);
                }

            this.shape = new HeightfieldCollisionShape(heights);
        }

        return shape;
    }

    @Override
    public String getName() {
        return "Heightfield Collider";
    }

    public interface IHeightfieldHandler {
        float run(float x, float z);
    }

}
