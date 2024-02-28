package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.timer.Timer;
import org.joml.FrustumIntersection;

public class MeshComponent extends Component {
    public boolean enableRendering = true;

    public IMesh mesh;

    private float lastCameraDistance;
    private int qualityUpdateTicks;
    private int qualityDistance = 0;

    public MeshComponent(IMesh mesh) {
        this.mesh = mesh;
    }

    /**
     * Determines how far the mesh must be from the camera for the quality level to change.
     * Value in range from 1 to 10, where 1 is the default value and the quality would change at normal distance,
     * and where 10 is the quality of the mesh would change being too far away from the mesh.
     * </p>
     * If the value is set to 0, the quality of the mesh would not be changed based on the distance (the default value).
     * */
    public void setQualityDistance(int qualityDistance) {
        this.qualityDistance = Math.min(10, Math.max(1, qualityDistance));
    }

    @Override
    public void tick(Timer timer) {
        // Set mesh's quality based on the distance from the camera.
        CameraNode camera = getNode().getScene().getMainCamera();
        FrustumIntersection frustum = getEngine().getRenderer().getViewport().getFrustum();
        TransformComponent nodeTransform = getNode().getTransform();
        SceneNode node = getNode();

        if (node == null || this.qualityDistance == 0)
            return;

        if (mesh != null && camera != null && node.getLayer().equals(BaseScene.Layers.MAIN)) {
            float cameraDistance = camera.getTransform().position.distance(nodeTransform.position);
            if (qualityUpdateTicks++ % ((int)(timer.getTickPerSecond() / 2)) == 0) {
                // Do that only if the mesh is not in the camera viewport and the level of quality is going to degrade,
                // because it'd look bad if object would just disappear
                if (cameraDistance < lastCameraDistance || !frustum.testPoint(nodeTransform.position)) {
                    float renderDistance = Engine.getInstance().getOptions().getFloat(Options.Values.RENDER_DISTANCE);
                    float qualityMod0 = (80 * this.qualityDistance) * renderDistance,
                            qualityMod1 = (160 * this.qualityDistance) * renderDistance,
                            qualityMod2 = (260 * this.qualityDistance) * renderDistance;

                    if (renderDistance == 0) {
                        this.mesh.setQuality(cameraDistance < 50 ? MeshQuality.POTATO : MeshQuality.NOT_RENDERED);
                    }
                    else {
                        this.mesh.setQuality(
                                cameraDistance < (50 + qualityMod0) ? MeshQuality.HIGH :
                                cameraDistance < (120 + qualityMod1) ? MeshQuality.MEDIUM :
                                cameraDistance < (140 + qualityMod2) ? MeshQuality.LOW : MeshQuality.NOT_RENDERED);
                        this.lastCameraDistance = cameraDistance;
                    }
                }
            }
        }
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

            if (!mesh.isBaked() || mesh.isDirty())
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
