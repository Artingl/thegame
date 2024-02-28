package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.Intractable;
import dev.artingl.Game.scene.Models;

public class DingusNode extends SpriteNode implements Intractable {

    public DingusNode() {
        super(new ModelMesh(Models.DINGUS, "dingus", "whiskers"));
        this.addComponent(new MeshColliderComponent(getMesh()));
        this.addComponent(new RigidBodyComponent());
//        this.addComponent(new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/dingus.ogg"))));
    }

    @Override
    public void clicked() {

    }

    @Override
    public Text getName() {
        return new Text(new Resource("thegame", "en/object.dingus"));
    }
}
