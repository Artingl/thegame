package dev.artingl.Engine.scene.components;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.scene.components.transform.TransformComponent;
import dev.artingl.Engine.scene.nodes.CameraNode;
import dev.artingl.Engine.scene.nodes.SceneNode;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.timer.Timer;
import org.joml.FrustumIntersection;

public class MeshComponent extends Component {
    public boolean enableRendering = true;

    public IMesh mesh;

    private float lastCameraDistance;
    private int qualityUpdateTicks;

    public MeshComponent(IMesh mesh) {
        this.mesh = mesh;
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
    public void tick(Timer timer) {
        // Set mesh's quality based on the distance from the camera.
        CameraNode camera = getNode().getScene().getMainCamera();
        FrustumIntersection frustum = getEngine().getRenderer().getViewport().getFrustum();
        TransformComponent nodeTransform = getNode().getTransform();

        if (mesh != null && camera != null) {
            float cameraDistance = camera.getTransform().position.distance(nodeTransform.position);
            if (qualityUpdateTicks++ % timer.getTickPerSecond() == 0) {
                // Do that only if the mesh is not in the camera viewport and the level of quality is going to degrade,
                // because it'd look bad if object would just disappear
                if (cameraDistance < lastCameraDistance || !frustum.testPoint(nodeTransform.position)) {
                    float renderDistance = Engine.getInstance().getOptions().getFloat(Options.Values.RENDER_DISTANCE);
                    float qualityMod0 = 80 * renderDistance,
                            qualityMod1 = 160 * renderDistance,
                            qualityMod2 = 260 * renderDistance;

                    if (renderDistance == 0) {
                        this.mesh.setQuality(cameraDistance < 50 ? MeshQuality.POTATO : MeshQuality.NOT_RENDERED);
                    }
                    else {
                        this.mesh.setQuality(cameraDistance < (50 + qualityMod0) ? MeshQuality.HIGH : cameraDistance < (150 + qualityMod1) ? MeshQuality.MEDIUM : cameraDistance < (250 + qualityMod2) ? MeshQuality.LOW : MeshQuality.NOT_RENDERED);
                        this.lastCameraDistance = cameraDistance;
                    }
                }
            }
        }
    }

    @Override
    public void disable() {
        enableRendering = false;
    }

    @Override
    public void enable() {
        enableRendering = true;
    }

    @Override
    public void cleanup() {
        if (mesh == null)
            return;

        if (mesh.isBaked())
            mesh.cleanup();
    }

    @Override
    public void render(SceneNode node, RenderContext context) {
        if (mesh != null && enableRendering) {
            TransformComponent transform = node.getTransform();

            if (!mesh.isBaked() | mesh.isDirty())
                mesh.bake();

            mesh.transform(transform.getMatrix());
            mesh.render(context);
        }
    }

    @Override
    public String getName() {
        return "Mesh Renderer";
    }
}
