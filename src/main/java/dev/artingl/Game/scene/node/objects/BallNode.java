package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.SphereColliderComponent;
import dev.artingl.Game.scene.node.IntractableNode;

public class BallNode extends IntractableNode {

    public BallNode() {
        IMesh mesh = new SphereMesh(Color.WHITE, Texture.get(new Resource("thegame", "objects/ball")), 0.8f);
        this.addComponent(new MeshComponent(mesh));
        this.addComponent(new SphereColliderComponent(0.8f));
        this.addComponent(new RigidBodyComponent(0.5f));
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
