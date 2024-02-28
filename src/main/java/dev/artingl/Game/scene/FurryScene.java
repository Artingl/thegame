package dev.artingl.Game.scene;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.FurryRendererComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.PlaneSprite;
import dev.artingl.Engine.world.scene.nodes.sprites.SphereSprite;
import dev.artingl.Game.scene.node.PlayerControllerNode;
import org.joml.Vector3f;

public class FurryScene extends BaseScene {

    private final PlayerControllerNode playerController;

    public FurryScene() {
        this.playerController = new PlayerControllerNode();
        this.playerController.captureControl = false;
        this.playerController.getCamera().backgroundColor = Color.from(50, 50, 200);

        TransformComponent transform = this.playerController.getTransform();
        transform.position = new Vector3f(0, 5, 10);
        transform.rotation = new Vector3f(30, 0, 0);
        this.addNode(this.playerController);

        SphereSprite sprite = new SphereSprite(1);
        sprite.getTransform().position.x = 0;
        sprite.getTransform().position.y = 2;
        sprite.getTransform().position.z = 0;
//        sprite.addComponent(new FurryRendererComponent(FurryRendererComponent.Type.MESH, 32, 2048, 0.001f));
        this.addNode(sprite);

        PlaneSprite plane = new PlaneSprite(100, 100);
        plane.getTransform().position.x = 0;
        plane.getTransform().position.y = 0;
        plane.getTransform().position.z = 0;
        plane.addComponent(new MeshColliderComponent(plane.getMesh()));
        plane.addComponent(new RigidBodyComponent());
        plane.addComponent(new FurryRendererComponent(FurryRendererComponent.Type.PLANE, 64, 1024, 0.005f));
        this.addNode(plane);
    }

}
