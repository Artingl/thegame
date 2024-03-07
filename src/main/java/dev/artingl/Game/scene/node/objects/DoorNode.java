package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.phys.GhostBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Game.scene.Models;
import dev.artingl.Game.scene.node.IntractableNode;

public class DoorNode extends IntractableNode {

    private float targetRotation = 0;

    public DoorNode() {
        // TODO: mesh collider for a door is stupid
        IMesh mesh = new ModelMesh(Models.DOOR);
        this.addComponent(new MeshComponent(mesh));
        this.addComponent(new MeshColliderComponent(mesh));
        this.addComponent(new RigidBodyComponent(0));
        getTransform().pivot.x = -1.7f;
    }

    @Override
    public Text getTooltipTitle() {
        return new Text(new Resource("thegame", "en/object.open.door"));
    }

    @Override
    public void interact(SceneNode node) {
        super.interact(node);
        if (this.targetRotation != 0) this.targetRotation = 0;
        else this.targetRotation = 90;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        getTransform().rotation.y = targetRotation + (getTransform().rotation.y - targetRotation) * (120 / timer.getTickPerSecond());
    }
}
