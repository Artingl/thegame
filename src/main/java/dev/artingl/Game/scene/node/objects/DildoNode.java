package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.renderer.mesh.BoxMesh;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.BoxColliderComponent;
import dev.artingl.Game.scene.Models;
import dev.artingl.Game.scene.node.IntractableNode;
import org.joml.Vector3f;

public class DildoNode extends IntractableNode {

    public DildoNode() {
        IMesh mesh = new ModelMesh(Models.DILDO);
        this.addComponent(new MeshComponent(mesh));
        this.addComponent(new BoxColliderComponent(new Vector3f(0.5f, 1.5f, 0.5f)));
        this.addComponent(new RigidBodyComponent());
    }

    @Override
    public Text getTooltipTitle() {
        return new Text(new Resource("thegame", "en/object.pick_up.ball"));
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
