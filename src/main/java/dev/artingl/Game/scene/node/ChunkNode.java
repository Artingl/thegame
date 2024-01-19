package dev.artingl.Game.scene.node;

import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.scene.components.MeshComponent;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Game.level.chunk.Chunk;
import org.joml.FrustumIntersection;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class ChunkNode extends SceneNode {

    private final Chunk chunk;

    public ChunkNode(Chunk chunk) {
        this.chunk = chunk;

        MeshComponent mesh = new MeshComponent(chunk.getMesh());
//        TerrainColliderComponent collider = new TerrainColliderComponent(
//                Chunk.CHUNK_SIZE, Chunk.CHUNK_SIZE,
//                (x, z) -> chunk.getGenerator().getHeight(chunk.getPositionLevel().x + x, chunk.getPositionLevel().y + z)
//        );

        this.addComponent(mesh);
//        this.addComponent(collider);
    }

    /**
     * Get assigned chunk to the node.
     * */
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void render(RenderContext context) {
        // Check if the chunk is visible by the camera or not inside render distance
        FrustumIntersection frustum = context.getViewport().getFrustum();
        IViewport viewport = context.getViewport().getCurrentViewport();
        if (viewport == null) {
            // We don't have any camera
            this.abortRender();
            return;
        }

        Vector2i cameraPos = new Vector2i((int) viewport.getPosition().x, (int) viewport.getPosition().z);
        Vector2i chunkPos = chunk.getPositionLevel();

        if (cameraPos.distance(chunkPos) > viewport.getZFar() * 1.03f) {
            // Chunk is too far
            this.abortRender();
            return;
        }

        if (frustum.intersectAab(
                new Vector3f(chunkPos.x, 0, chunkPos.y),
                new Vector3f(chunkPos.x + Chunk.CHUNK_SIZE, 256, chunkPos.y + Chunk.CHUNK_SIZE)) >= 0) {
            this.abortRender();
            return;
        }

        super.render(context);
    }
}
