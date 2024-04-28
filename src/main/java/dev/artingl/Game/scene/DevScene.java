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
import dev.artingl.Engine.world.scene.components.transform.InstancedTransformComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.nodes.ui.UIPanelNode;
import dev.artingl.Engine.world.scene.nodes.sprites.SphereSprite;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.Chunk;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import dev.artingl.Game.scene.node.*;
import dev.artingl.Game.scene.node.ambient.ChunkNode;
import dev.artingl.Game.scene.node.ambient.EnvNode;
import dev.artingl.Game.scene.node.ambient.SkyNode;
import dev.artingl.Game.scene.node.objects.*;
import dev.artingl.Game.scene.node.ui.CrosshairNode;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.Collection;

public class DevScene extends BaseScene {

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
    private final CrosshairNode crosshair;
    private final SoundComponent nightAmbientSound;
    private final SoundComponent dayAmbientSound;

    public DevScene() {
        Level level = getLevel();

        SphereSprite testSphere = new SphereSprite(1, Color.from((int) (14.75f * 255), (int) (9.12f * 255), (int) (1.71f * 255)), null);
        testSphere.getTransform().position.x = 10;
        testSphere.getTransform().position.y = 6;
        testSphere.getTransform().position.z = -18;
        this.addNode(testSphere);

        /* Initialize and add nodes to the scene */
        this.sky = new SkyNode(level.getSky());
        this.uiPanel = new UIPanelNode();
        this.playerController = new PlayerControllerNode();
        this.playerController.captureControl = false;

        TransformComponent transform = this.playerController.getTransform();
        transform.position = new Vector3f(0, 5, 0);
        transform.rotation = new Vector3f(0, 90, 0);

        this.addNode(this.uiPanel);
        this.addNode(this.playerController);
        this.addNode(this.sky);

        // Shelter node
        this.shelterNode = new ShelterNode();
        this.addNode(this.shelterNode);

        // Dingus
        this.dingusNode = new DingusNode();
        this.dingusNode.getTransform().position.x = -7;
        this.dingusNode.getTransform().position.y = 7;
        this.dingusNode.getTransform().position.z = -14;
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

        // Initialize the UI
        this.crosshair = new CrosshairNode();
        this.addChild(this.uiPanel, crosshair);

        BallNode ball = new BallNode();
        ball.getTransform().position.x = 20;
        ball.getTransform().position.y = 7;
        ball.getTransform().position.z = 7;
        this.addNode(ball);

        TableNode table = new TableNode();
        table.getTransform().position.x = 25;
        table.getTransform().position.y = 7;
        table.getTransform().position.z = 8;
        table.getTransform().rotation.y = 90;
        this.addNode(table);

        for (int i = 0; i < 256; i++) {
            BallNode ball0 = new BallNode();
            ball0.getTransform().position.x = Utils.randInt(-2, 2);
            ball0.getTransform().position.y = 10;
            ball0.getTransform().position.z = Utils.randInt(-2, 2);
            this.addNode(ball0);
        }

        LaptopNode laptop0 = new LaptopNode();
        laptop0.getTransform().position.x = -10;
        laptop0.getTransform().position.y = 2.2f;
        laptop0.getTransform().position.z = 20;
        this.addNode(laptop0);

        LaptopNode laptop1 = new LaptopNode();
        laptop1.getTransform().position.x = -10;
        laptop1.getTransform().position.y = 2.2f;
        laptop1.getTransform().position.z = 14;
        this.addNode(laptop1);

        this.makeEnvironment();
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
            EnvNode[] envObjectNodes = new EnvNode[EnvironmentObjects.values().length];
            for (Pair<EnvironmentObjects, Vector3f> pair : chunk.getEnvObjectsList()) {
                EnvironmentObjects obj = pair.getA();
                Vector3f position = pair.getB();
                Vector3f rotation = new Vector3f(0, Utils.randInt(0, 360), 0);
                EnvNode envNode = envObjectNodes[obj.ordinal()];
                int objId = obj.ordinal();

                /* Initialize the node for the object if we haven't yet */
                if (envNode == null) {
                    envNode = envObjectNodes[objId] = new EnvNode(node);
                    IModel model = Models.MODELS[objId];
                    ModelMesh mesh = new ModelMesh(model);

                    // Disable fade animations for trees because it'd look awful
                    if (obj == EnvironmentObjects.TREE)
                        mesh.toggleFade(false);

                    InstancedMeshComponent meshComponent = new InstancedMeshComponent(mesh);
                    meshComponent.setQualityDistance(2);

                    envNode.addComponent(meshComponent);
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

    public boolean test = true;

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        this.shelterNode.isEnabled = test;
        getLevel().setTimeSpeed(timeSpeed);

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
        this.levelCachedHeights = getLevel().getGenerator().getCacheSize();
    }

    public Level getLevel() {
        GameDirector director = GameDirector.getInstance();
        return director.getLevelsRegistry().getCurrentLevel();
    }

    public PlayerControllerNode getPlayerController() {
        return playerController;
    }

    public ShelterNode getShelter() {
        return shelterNode;
    }

    public CrosshairNode getCrosshair() {
        return crosshair;
    }
}
