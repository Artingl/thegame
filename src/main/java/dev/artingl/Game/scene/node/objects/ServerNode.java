package dev.artingl.Game.scene.node.objects;

import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.audio.SoundBuffer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.SoundComponent;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.scene.DevScene;
import dev.artingl.Game.scene.Models;

public class ServerNode extends SpriteNode {

    private final SoundComponent sound;

    public ServerNode() {
        super(new ModelMesh(Models.SERVER));
        this.sound = new SoundComponent(new SoundBuffer(new Resource("thegame", "audio/server0.ogg")));
        this.sound.getSound().setLoop(true);
        this.sound.volume = 0;

        this.addComponent(new MeshColliderComponent(getMesh()));
        this.addComponent(new RigidBodyComponent());
        this.addComponent(sound);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        // Mute the server's sound if player is not inside the shelter
        BaseScene scene = getScene();

        if (scene instanceof DevScene map) {
            ShelterNode shelter = map.getShelter();

            float volumeChangeSpeed = 3 / timer.getTickPerSecond();
            float targetVolume = shelter.isPlayerInside() ? 0.6f : 0.1f;
            this.sound.volume = this.sound.volume + (targetVolume - this.sound.volume) * volumeChangeSpeed;
        }
    }
}
