package dev.artingl.Game.scene;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.renderer.models.IModel;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.audio.SoundBuffer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.InstancedMeshComponent;
import dev.artingl.Engine.world.scene.components.SoundComponent;
import dev.artingl.Engine.world.scene.components.annotations.ComponentFinalField;
import dev.artingl.Engine.world.scene.components.transform.InstancedTransformComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.world.scene.nodes.sprites.SphereSprite;
import dev.artingl.Engine.world.scene.nodes.ui.UIPanelNode;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.Chunk;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import dev.artingl.Game.scene.node.PlayerControllerNode;
import dev.artingl.Game.scene.node.ambient.ChunkNode;
import dev.artingl.Game.scene.node.ambient.EnvNode;
import dev.artingl.Game.scene.node.ambient.SkyNode;
import dev.artingl.Game.scene.node.objects.*;
import dev.artingl.Game.scene.node.ui.CrosshairNode;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.Collection;

public class GameScene extends BaseScene {

    public int timeSpeed = 1;

    @ComponentFinalField
    public String terrainType = "UNDEFINED";

    @ComponentFinalField
    public int levelCachedHeights = -1;

    private final SkyNode sky;
    private final UIPanelNode uiPanel;
    private final PlayerControllerNode playerController;
    private final ApartmentsNode apartments;
    private final CrosshairNode crosshair;

    public GameScene() {
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
        transform.position = new Vector3f(0, 16, 0);
        transform.rotation = new Vector3f(0, 90, 0);

        this.apartments = new ApartmentsNode();

        this.addNode(this.apartments);
        this.addNode(this.uiPanel);
        this.addNode(this.playerController);
        this.addNode(this.sky);

        TableNode table = new TableNode();
        table.getTransform().position.x = 4;
        table.getTransform().position.y = 16;
        table.getTransform().position.z = 4;
        table.getTransform().rotation.y = 90;
        this.addNode(table);

        BallNode ball = new BallNode();
        ball.getTransform().position.x = 0;
        ball.getTransform().position.y = 16;
        ball.getTransform().position.z = 5;
        this.addNode(ball);

        // Initialize the UI
        this.crosshair = new CrosshairNode();
        this.addChild(this.uiPanel, crosshair);

        getEngine().getSoundsManager().setGlobalVolume(0.3f);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        getLevel().setTimeSpeed(timeSpeed);

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

    public CrosshairNode getCrosshair() {
        return crosshair;
    }
}
