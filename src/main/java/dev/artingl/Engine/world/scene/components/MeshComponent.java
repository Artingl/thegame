package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.Quality;
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
     * Determines how fast the mesh's quality would degrade based on its distance from the camera.
     *
     * @param qualityDistance Value in the range from 1 to 10
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
        Engine engine = getEngine();

        if (node == null || this.qualityDistance == 0)
            return;

        if (mesh != null && camera != null && node.getLayer().equals(BaseScene.Layer.MAIN)) {
            float cameraDistance = camera.getTransform().position.distance(nodeTransform.position);
            if (qualityUpdateTicks++ % ((int)(timer.getTickPerSecond() / 2)) == 0) {
                // Do that only if the mesh is not in the camera viewport and the level of quality is going to degrade,
                // because it'd look bad if object would just disappear
                if (cameraDistance < lastCameraDistance || !frustum.testPoint(nodeTransform.position)) {
                    Quality highestSetting = (Quality) engine.getOptions().get(Options.Values.QUALITY_SETTING);
                    float renderDistance = engine.getOptions().getFloat(Options.Values.RENDER_DISTANCE);
                    float qualityMod0 = (80 * this.qualityDistance) * renderDistance,
                            qualityMod1 = (160 * this.qualityDistance) * renderDistance,
                            qualityMod2 = (260 * this.qualityDistance) * renderDistance;

                    if (renderDistance == 0) {
                        this.mesh.setQuality(cameraDistance < 50 ? Quality.clamp(highestSetting, Quality.POTATO) : Quality.clamp(highestSetting, Quality.NOT_RENDERED));
                    }
                    else {
                        this.mesh.setQuality(
                                cameraDistance < (50 + qualityMod0) ? Quality.clamp(highestSetting, Quality.HIGH) :
                                cameraDistance < (120 + qualityMod1) ? Quality.clamp(highestSetting, Quality.MEDIUM) :
                                cameraDistance < (140 + qualityMod2) ? Quality.clamp(highestSetting, Quality.LOW) : Quality.clamp(highestSetting, Quality.NOT_RENDERED));
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
    public void render(SceneNode node, Renderer renderer) {
        if (mesh != null && enableRendering) {
            TransformComponent transform = node.getTransform();

            if (!mesh.isBaked() || mesh.isDirty())
                mesh.bake();

            mesh.transform(transform.getMatrix());
            mesh.render(renderer);
        }
    }

    @Override
    public String getName() {
        return "Mesh Renderer";
    }
}
