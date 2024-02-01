package dev.artingl.Game.scene;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.models.IModel;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.annotations.ComponentFinalField;
import dev.artingl.Engine.world.scene.components.InstancedMeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.SphereColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.TerrainColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.InstancedTransformComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.nodes.sprites.SphereNode;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.Chunk;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import dev.artingl.Game.scene.node.*;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.Collection;

public class MapScene extends BaseScene {

    public int timeSpeed = 1;

    @ComponentFinalField
    public String terrainType = "UNDEFINED";

    @ComponentFinalField
    public int levelCachedHeights = -1;

    private final SkyNode sky;
    private final CameraControlNode cameraController;
    private final ShelterNode shelterNode;
    private final DingusNode dingusNode;

    private final SphereNode sphere;

    public MapScene() {
        Level level = getLevel();

        /* Initialize and add nodes to the scene */
        this.sky = new SkyNode(level.getSky());
        this.cameraController = new CameraControlNode();
        this.cameraController.captureControl = false;

        TransformComponent transform = this.cameraController.getTransform();
        transform.position = new Vector3f(0, 10, 50);
//        transform.rotation = new Vector3f(60, -18, 0);

        this.addNode(this.cameraController);
        this.addNode(this.sky);

        // Shelter node
        this.shelterNode = new ShelterNode();
        this.shelterNode.getTransform().position.y = 1f;
        this.addNode(this.shelterNode);

        // Dingus
        this.dingusNode = new DingusNode();
        this.dingusNode.getTransform().position.y = 1.8f;
//        this.addNode(dingusNode);

        this.makeEnvironment();

        this.sphere = new SphereNode(1);
        this.sphere.getTransform().position.x = 5;
        this.sphere.getTransform().position.y = 20;
        this.sphere.getTransform().position.z = 5;
        this.sphere.addComponent(new SphereColliderComponent(1));
        this.sphere.addComponent(new RigidBodyComponent());
        this.addNode(sphere);

        getEngine().getSoundsManager().setGlobalVolume(1f);
    }

    public void makeEnvironment() {
        Level level = getLevel();

        /* Add all chunks as nodes from current level */
        Collection<Chunk> chunks = level.getChunks();
        for (Chunk chunk : chunks) {
            ChunkNode node = new ChunkNode(chunk);
            node.getTransform().position.set(chunk.getPositionLevel().x, 0, chunk.getPositionLevel().y);
            this.addNode(node);

            /* Since we'd need to render a huge amount of different kind objects on the scene (trees, etc.)
             * it'd too inefficient to make different draw call for each object (tried with ~2000 trees and got 5 fps).
             * So, instead we'd need to do so called "instanced rendering", which just means we need to provide
             * vertex info (position, rotation, scale), and render all the objects by just one draw call.
             * */
            SceneNode[] envObjectNodes = new SceneNode[EnvironmentObjects.values().length];
            for (Pair<EnvironmentObjects, Vector3f> pair : chunk.getEnvObjectsList()) {
                EnvironmentObjects obj = pair.getA();
                Vector3f position = pair.getB();
                Vector3f rotation = new Vector3f(0, Utils.randInt(0, 360), 0);
                SceneNode envNode = envObjectNodes[obj.ordinal()];
                int objId = obj.ordinal();

                /* Initialize the node for the object if we haven't yet */
                if (envNode == null) {
                    envNode = envObjectNodes[objId] = new SceneNode();
                    IModel model = Models.MODELS[objId];
                    ModelMesh mesh = new ModelMesh(model);

                    // Disable fade animations for trees because it'd look awful
                    if (obj == EnvironmentObjects.TREE)
                        mesh.toggleFade(false);

                    envNode.addComponent(new InstancedMeshComponent(mesh));
                    envNode.changeTransformInstance(new InstancedTransformComponent());
                    envNode.setNametag("MODEL_INSTANCE_" + model.getResource());
                    envNode.getTransform().position = node.getTransform().position;
                    this.addChild(node.getUUID(), envNode);
                }

                /* Add the position of the object to the instanced transform, which will later
                 * tell the renderer at what locating we want to render the object
                 */
                InstancedTransformComponent transform = ((InstancedTransformComponent) (envNode.getTransform()));
                transform.addInstanceTransform(position, rotation, new Vector3f(1));
            }
        }
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        // Rotate sky (360 degrees rotation every Level.DAY_DURATION_TICKS ticks)
        this.sky.getTransform().rotation.z = getLevel().getSky().getSunRotation();

        // Set sky node position relative to current camera position
        this.sky.getTransform().position = this.cameraController.getTransform().position;

        this.getLevel().getSky().setColor(Color.from("#5b88b5"));
        this.getLevel().setTimeSpeed(timeSpeed);

        // ...
        this.terrainType = getLevel()
                .getGenerator()
                .getTerrainTypeAt(
                        cameraController.getTransform().position.x,
                        cameraController.getTransform().position.z
                ).name();
        this.levelCachedHeights = getLevel().getGenerator().getCacheSize();
    }

    private Level getLevel() {
        GameDirector director = GameDirector.getInstance();
        return director.getLevelsRegistry().getCurrentLevel();
    }
}
