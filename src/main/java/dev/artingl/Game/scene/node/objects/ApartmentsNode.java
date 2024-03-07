package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Models;

public class ApartmentsNode extends SpriteNode {

    public ApartmentsNode() {
        super(new ModelMesh(Models.APARTMENTS));
        this.addComponent(new MeshColliderComponent(getMesh()));
        this.addComponent(new RigidBodyComponent());

        MeshComponent meshComponent = getComponent(MeshComponent.class);
        ((ModelMesh)meshComponent.mesh).toggleFade(false);
    }

    @Override
    public void init() throws EngineException {
        super.init();

        BaseScene scene = getScene();
        DoorNode door0 = new DoorNode();
        door0.getTransform().position.x = 9.5f;
        door0.getTransform().position.y = 13;
        door0.getTransform().position.z = -11;
        scene.addChild(this, door0);

//        DoorNode door1 = new DoorNode();
//        door1.getTransform().position.x = 13.9f;
//        door1.getTransform().position.y = 13;
//        door1.getTransform().position.z = -6;
//        door1.getTransform().rotation.y = 90;
//        scene.addChild(this, door1);
    }
}
