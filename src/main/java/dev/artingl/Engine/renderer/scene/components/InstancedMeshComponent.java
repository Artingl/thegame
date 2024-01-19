package dev.artingl.Engine.renderer.scene.components;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.renderer.scene.components.transform.InstancedTransformComponent;
import dev.artingl.Engine.renderer.scene.components.transform.TransformComponent;
import dev.artingl.Engine.renderer.scene.nodes.CameraNode;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;

public class InstancedMeshComponent extends MeshComponent {

    private int lastTransformsHash = -1;

    public InstancedMeshComponent(IMesh mesh) {
        super(mesh);
    }

    @Override
    public void init(SceneNode node) throws EngineException {
        super.init(node);

        if (mesh == null)
            return;

        if (!mesh.isBaked())
            mesh.bake();
    }

    @Override
    public void render(SceneNode node, RenderContext context) {
        if (mesh != null && enableRendering) {
            if (getNode().getTransform() instanceof InstancedTransformComponent transform) {
                // Update transform instances for the mesh if they were updated
                if (transform.getTransformsHash() != lastTransformsHash || mesh.isDirty()) {
                    this.lastTransformsHash = transform.getTransformsHash();
                    this.mesh.clearInstances();
                    for (TransformComponent instance: transform.getTransforms())
                        this.mesh.addInstance(instance.getMatrix());
                    this.mesh.bake();
                }
                this.mesh.renderInstanced(context);
            } else {
                Engine.getInstance().getLogger().log(LogLevel.WARNING, "Instanced Mesh Renderer can work only with Instanced Transform Component!");
            }
        }
    }

    @Override
    public String getName() {
        return "Instanced Mesh Renderer";
    }
}
