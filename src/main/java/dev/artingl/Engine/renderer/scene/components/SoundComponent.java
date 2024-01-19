package dev.artingl.Engine.renderer.scene.components;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.audio.SoundBuffer;
import dev.artingl.Engine.audio.SoundSource;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;

public class SoundComponent extends Component {

    // TODO: control volume thru component interface (in debugger)

    @ComponentFinalField
    public boolean isPlaying;
    public float volume;

    private final SoundSource source;

    public SoundComponent(SoundBuffer buffer) {
        this.source = new SoundSource(buffer, new Vector3f(0, 0, 0));
        this.volume = 1;
        this.isPlaying = false;
    }

    public SoundSource getSound() {
        return source;
    }

    @Override
    public void init(SceneNode node) throws EngineException {
        super.init(node);
        getEngine().getSoundsManager().addSource(this.source);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        getEngine().getSoundsManager().removeSource(this.source);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        if (getNode() != null) {
            SceneNode node = getNode();
            if (source.isPlaying() && !node.isEnabled)
                source.pause();
            if (!source.isPlaying() && node.isEnabled)
                source.play();

//            this.source.setPosition(new Vector3f(node.getTransform().position));
//            this.source.setVolume(this.volume);
//            this.isPlaying = this.source.isPlaying();
        }
    }
}
