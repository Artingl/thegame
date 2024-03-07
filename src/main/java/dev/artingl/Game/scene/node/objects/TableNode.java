package dev.artingl.Game.scene.node.objects;

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

public class TableNode extends IntractableNode {


    public TableNode() {
        IMesh mesh = new ModelMesh(Models.TABLE);
        this.addComponent(new MeshComponent(mesh));
        this.addComponent(new BoxColliderComponent(new Vector3f(2.52032f/2f, 2.5272f/2f, 7.85354f/2f)));
        this.addComponent(new RigidBodyComponent(10));
    }

    @Override
    public Text getTooltipTitle() {
        return new Text(new Resource("thegame", "en/object.drag.table"));
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
