package dev.artingl.Engine.audio;

import dev.artingl.Engine.misc.world.Dimension;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private final SoundBuffer buffer;
    private Dimension dimension;
    private Vector3f position;
    private int sourceId;

    public SoundSource(SoundBuffer buffer, Dimension dimension, Vector3f position) {
        this.buffer = buffer;
        this.dimension = dimension;
        this.position = position;
        this.sourceId = -1;
    }

    /**
     * Change sound's dimension
     * */
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     * Init the sound
     * */
    public void init() throws Exception {
        stop();
        this.buffer.init();
        if (sourceId == -1)
            this.sourceId = alGenSources();
        alSourcei(sourceId, AL_BUFFER, this.buffer.getBufferId());
        alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_FALSE);
    }

    /**
     * Pause the sound
     * */
    public void pause() {
        if (sourceId != -1)
            alSourcePause(sourceId);
    }

    /**
     * Stop the sound
     * */
    public void stop() {
        if (sourceId != -1)
            alSourceStop(sourceId);
    }

    /**
     * Change sound position
     * */
    public void setPosition(Vector3f position) {
        if (!this.position.equals(position)) {
            this.position = position;
            stop();
            play();
        }
    }

    /**
     * Get sound's position in the world
     * */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Get sound's dimension
     * */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Set sound's volume
     *
     * @param volume New volume value
     */
    public void setVolume(float volume) {
        if (sourceId != -1)
            alSourcef(sourceId, AL_GAIN, volume);
    }

    /**
     * Manually play the sound
     * */
    public void play() {
        play(position);
    }

    /**
     * Manually play the sound ad co-ordinates
     *
     * @param pos Position where the sound will be played
     * */
    public void play(Vector3f pos) {
        if (sourceId == -1)
            return;

        // Set the sound's position and play it
        alSource3f(sourceId, AL_POSITION, pos.x, pos.y, pos.z);
        alSourcePlay(sourceId);
    }

    /**
     * Is the sound playing right now
     * */
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    /**
     * Sets OpenAL param for the source.
     *
     * @param param The param enum value
     * @param value The value to be set
     * */
    public void setParam(int param, boolean value) {
        if (this.sourceId != -1) {
            alSourcei(this.sourceId, param, value ? AL_TRUE : AL_FALSE);
        }
    }

    public void cleanup() {
        stop();
        if (this.sourceId != -1) {
            alDeleteSources(this.sourceId);
            this.sourceId = -1;
        }
        this.buffer.cleanup();
    }

}
