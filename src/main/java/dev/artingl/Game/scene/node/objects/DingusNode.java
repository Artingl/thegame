package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.renderer.mesh.BoxMesh;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.world.audio.SoundBuffer;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.SoundComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.BoxColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Game.scene.Models;
import dev.artingl.Game.scene.node.IntractableNode;
import org.joml.Vector3f;

public class DingusNode extends IntractableNode {


    public DingusNode() {
        IMesh mesh = new ModelMesh(Models.DINGUS, "dingus", "whiskers");
        this.addComponent(new MeshComponent(new BoxMesh(new Vector3f(2.0f, 1.4f, 1.2f))));
        this.addComponent(new MeshComponent(mesh));
//        this.addComponent(new MeshColliderComponent(mesh));
        this.addComponent(new BoxColliderComponent(new Vector3f(2.0f, 1.4f, 1.2f)));
        this.addComponent(new RigidBodyComponent());
        this.addComponent(new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/dingus.ogg"))));
    }

    @Override
    public Text getTooltipTitle() {
        return new Text(new Resource("thegame", "en/object.pick_up.dingus"));
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
