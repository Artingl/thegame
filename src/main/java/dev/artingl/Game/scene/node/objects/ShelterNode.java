package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.MapScene;
import dev.artingl.Game.scene.Models;
import dev.artingl.Game.scene.node.PlayerControllerNode;

public class ShelterNode extends SpriteNode {

    public ShelterNode() {
        super(new ModelMesh(Models.SHELTER));
        this.addComponent(new MeshColliderComponent(getMesh()));
        this.addComponent(new RigidBodyComponent());

        MeshComponent meshComponent = getComponent(MeshComponent.class);
        ((ModelMesh)meshComponent.mesh).toggleFade(false);
    }

    @Override
    public void init() throws EngineException {
        super.init();

        for (int z = 31; z > 0; z -= 6) {
            ServerNode server = new ServerNode();
            server.getTransform().position.x = -24;
            server.getTransform().position.y = 1;
            server.getTransform().position.z = z;
            this.getScene().addChild(this, server);
        }
    }

    public boolean isPlayerInside() {
        BaseScene scene = getScene();

        // The player node should be on the map scene, and we expect shelter node to be only on that scene
        if (scene instanceof MapScene map) {
            PlayerControllerNode player = map.getPlayerController();
            TransformComponent playerTransform = player.getTransform();
            return playerTransform.position.x > -30 && playerTransform.position.x < 30
                    && playerTransform.position.z > -38 && playerTransform.position.z < 38;
        }

        return false;
    }

}
