package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.math.Vector3f;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;

public class TerrainColliderComponent extends BaseColliderComponent {

    private final int width;
    private final int depth;
    private final ITerrainHeightHandler handler;

    private HeightfieldCollisionShape shape;

    public TerrainColliderComponent(int width, int depth, ITerrainHeightHandler handler) {
        this.width = width;
        this.depth = depth;
        this.handler = handler;
    }

    public float getWidth() {
        return width;
    }

    public float getDepth() {
        return depth;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected CollisionShape buildCollider() {
        Engine.getInstance().getLogger().log(LogLevel.UNIMPLEMENTED, "Fix TerrainColliderComponent");

        if (shape == null) {
            float[] heights = new float[width*depth];
            for (int x = 0; x < width; x++)
                for (int z = 0; z < depth; z++) {
                    heights[z * width + x] = handler.run(x, z);
                }

            this.shape = new HeightfieldCollisionShape(heights);
        }

        return shape;
    }

    @Override
    public String getName() {
        return "Terrain Collider";
    }

    public interface ITerrainHeightHandler {
        float run(float x, float z);
    }

}
