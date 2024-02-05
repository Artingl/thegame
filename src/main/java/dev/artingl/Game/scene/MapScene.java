package dev.artingl.Game.scene;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.models.IModel;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.world.audio.SoundBuffer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.SoundComponent;
import dev.artingl.Engine.world.scene.components.annotations.ComponentFinalField;
import dev.artingl.Engine.world.scene.components.InstancedMeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.SphereColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.InstancedTransformComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.nodes.ui.UIPanelNode;
import dev.artingl.Engine.world.scene.nodes.sprites.SphereNode;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.Chunk;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import dev.artingl.Game.scene.node.*;
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
    private final UIPanelNode uiPanel;
    private final PlayerControllerNode playerController;
    private final ShelterNode shelterNode;
    private final DingusNode dingusNode;
    private final SoundComponent nightAmbientSound;
    private final SoundComponent dayAmbientSound;

    public MapScene() {
        Level level = getLevel();

        /* Initialize and add nodes to the scene */
        this.sky = new SkyNode(level.getSky());
        this.uiPanel = new UIPanelNode();
        this.playerController = new PlayerControllerNode();
        this.playerController.captureControl = false;

        TransformComponent transform = this.playerController.getTransform();
        transform.position = new Vector3f(0, 6.2f, -18);
        transform.rotation = new Vector3f(0, 90, 0);

        this.addNode(this.uiPanel);
        this.addNode(this.playerController);
        this.addNode(this.sky);

        // Shelter node
        this.shelterNode = new ShelterNode();
        this.shelterNode.getTransform().position.y = 1f;
        this.addNode(this.shelterNode);

        // Dingus
        this.dingusNode = new DingusNode();
        this.dingusNode.getTransform().position.y = 4f;
        this.addNode(dingusNode);

        // Ambient sounds
        SceneNode ambientSoundSource = new SceneNode();
        this.nightAmbientSound = new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/ambient_night0.ogg")));
        this.dayAmbientSound = new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/ambient_day0.ogg")));
        ambientSoundSource.addComponent(nightAmbientSound);
        ambientSoundSource.addComponent(dayAmbientSound);
        this.addNode(ambientSoundSource);

        // The ambient sounds should be global
        this.nightAmbientSound.getSound().setGlobal(true);
        this.dayAmbientSound.getSound().setGlobal(true);
        this.nightAmbientSound.getSound().setLoop(true);
        this.dayAmbientSound.getSound().setLoop(true);
        this.nightAmbientSound.volume = 0;
        this.dayAmbientSound.volume = 0;

        this.makeEnvironment();

        SphereNode sphere = new SphereNode(1);
        sphere.getTransform().position.x = 5;
        sphere.getTransform().position.y = 20;
        sphere.getTransform().position.z = 5;
        sphere.addComponent(new SphereColliderComponent(1));
        sphere.addComponent(new RigidBodyComponent(0.01f));
        this.addNode(sphere);

        LaptopNode laptop = new LaptopNode();
        laptop.getTransform().position.x = -10;
        laptop.getTransform().position.y = 2.2f;
        laptop.getTransform().position.z = 20;
        this.addNode(laptop);

        getEngine().getSoundsManager().setGlobalVolume(0.3f);
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
        this.sky.getTransform().position = this.playerController.getTransform().position;

        this.getLevel().getSky().setColor(Color.from("#5b88b5"));
        this.getLevel().setTimeSpeed(timeSpeed);

        // To know what ambient sound to play (night or day) we can look at current light level,
        // which would be 0 at night and 1 at day
        float nightMultiplier = Math.max(0, Math.min(1, 1 -  (getLevel().getLightLevel()*2)));
        float dayMultiplier = Math.max(0, Math.min(1, getLevel().getLightLevel()*2));

        // Adjust ambient sound whether the player is inside shelter or outside
        float volumeChangeSpeed = 3 / timer.getTickPerSecond();
        float targetAmbientVolume = shelterNode.isPlayerInside() ? 0.01f : 0.6f;
        this.nightAmbientSound.volume = (this.nightAmbientSound.volume + (targetAmbientVolume - this.nightAmbientSound.volume) * volumeChangeSpeed) * nightMultiplier;
        this.dayAmbientSound.volume = (this.dayAmbientSound.volume + (targetAmbientVolume - this.dayAmbientSound.volume) * volumeChangeSpeed) * dayMultiplier;

        // For debugger
        this.terrainType = getLevel()
                .getGenerator()
                .getTerrainTypeAt(
                        playerController.getTransform().position.x,
                        playerController.getTransform().position.z
                ).name();
        this.levelCachedHeights = getLevel().getGenerator().getCacheSize();
    }

    private Level getLevel() {
        GameDirector director = GameDirector.getInstance();
        return director.getLevelsRegistry().getCurrentLevel();
    }

    public PlayerControllerNode getPlayerController() {
        return playerController;
    }

    public ShelterNode getShelter() {
        return shelterNode;
    }
}
