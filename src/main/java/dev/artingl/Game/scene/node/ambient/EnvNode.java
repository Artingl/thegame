package dev.artingl.Game.scene.node.ambient;

import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.world.scene.nodes.SceneNode;

public class EnvNode extends SceneNode {

    private final ChunkNode chunk;

    public EnvNode(ChunkNode chunk) {
        this.chunk = chunk;
    }

    @Override
    public void render(Renderer renderer) {
        // Don't render the object if the assigned mesh to it is far from the camera (the quality is lower than MEDIUM)
//        if (chunk.getChunk().getMesh().getQuality().ordinal() > MeshQuality.MEDIUM.ordinal()) {
//            this.abortRender();
//            return;
//        }

        super.render(renderer);
    }
}
