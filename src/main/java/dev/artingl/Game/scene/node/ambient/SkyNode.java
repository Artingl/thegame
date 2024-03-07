package dev.artingl.Game.scene.node.ambient;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.ambient.Sky;
import dev.artingl.Game.scene.DevScene;
import dev.artingl.Game.scene.GameScene;
import dev.artingl.Game.scene.node.PlayerControllerNode;

public class SkyNode extends SceneNode {

    private final Sky sky;

    public SkyNode(Sky sky) {
        this.addComponent(new MeshComponent(sky.getMesh()));
        this.sky = sky;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        TransformComponent transform = getTransform();
        BaseScene scene = getScene();

        if (scene instanceof GameScene map) {
            Level level = map.getLevel();
            PlayerControllerNode playerController = map.getPlayerController();

            // Rotate sky (360 degrees rotation every Level.DAY_DURATION_TICKS ticks)
            transform.rotation.z = level.getSky().getSunRotation();

            // Set sky node position relative to current camera position
            transform.position = playerController.getTransform().position;

            level.getSky().setColor(Color.from("#5b88b5"));
        }
    }
}
