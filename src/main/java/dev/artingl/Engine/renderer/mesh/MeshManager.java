package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineEventListener;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.Renderer;

import java.util.concurrent.ConcurrentLinkedDeque;

public class MeshManager implements EngineEventListener {

    private final ConcurrentLinkedDeque<IMesh> activeMeshes;
    private final Logger logger;
    private final Renderer renderer;


    public MeshManager(Logger logger, Renderer renderer) {
        this.activeMeshes = new ConcurrentLinkedDeque<>();
        this.logger = logger;
        this.renderer = renderer;
    }

    public void init() {
        Engine.getInstance().subscribeEngineEvents(this);
    }

    public void cleanup() {
        Engine.getInstance().unsubscribeEngineEvents(this);
    }

    /**
     * Mark mesh as an active.
     * This allows to do certain features with meshes, e.g. receiving events (like reloading), etc.
     * <p>
     * Note: it's better to mark mesh as an inactive on its cleanup,
     * and after that mark active again when it's baked
     */
    public void activateMesh(IMesh mesh) {
        if (!this.activeMeshes.contains(mesh))
            this.activeMeshes.add(mesh);
    }

    /**
     * Mark mesh as an inactive
     * */
    public void deactivateMesh(IMesh mesh) {
        this.activeMeshes.remove(mesh);
    }

    @Override
    public void onReload() {
        this.logger.log(LogLevel.INFO, "Marking all active meshes as dirty");

        // Reload all meshes
        for (IMesh mesh: this.activeMeshes)
            mesh.reload();
    }
}
