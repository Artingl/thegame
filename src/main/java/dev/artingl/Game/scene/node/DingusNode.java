package dev.artingl.Game.scene.node;

import dev.artingl.Engine.audio.SoundBuffer;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.scene.components.SoundComponent;
import dev.artingl.Engine.scene.nodes.sprites.SpriteNode;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.scene.Models;

public class DingusNode extends SpriteNode {

    public DingusNode() {
        super(new ModelMesh(Models.DINGUS, "dingus", "whiskers"));
//        this.addComponent(new MeshColliderComponent());
//        this.addComponent(new RigidBodyComponent());
        this.addComponent(new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/dingus.ogg"))));
    }
}
