package dev.artingl.Engine.world.audio;

import dev.artingl.Engine.world.Dimension;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {

    private final SoundBuffer buffer;
    private Dimension dimension;
    private Vector3f position;
    private int sourceId;
    private float volume;
    private boolean isGlobal;
    private boolean isLooping;

    public SoundSource(SoundBuffer buffer, Dimension dimension, Vector3f position) {
        this.buffer = buffer;
        this.dimension = dimension;
        this.position = position;
        this.volume = 1;
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
            alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
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
        this.volume = volume;
        if (sourceId != -1)
            alSourcef(sourceId, AL_GAIN, this.volume);
    }

    /**
     * Returns current sound's volume
     * */
    public float getVolume() {
        return this.volume;
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
        alSourcei(sourceId, AL_SOURCE_RELATIVE, isGlobal ? 1 : 0);
        alSourcei(sourceId, AL_LOOPING, isLooping ? 1 : 0);
        alSourcef(sourceId, AL_GAIN, this.volume);
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

    /**
     * Sets global state of the sound. Global all listeners around the whole world would be able to hear it.
     * */
    public void setGlobal(boolean state) {
        this.isGlobal = state;
        if (sourceId != -1)
            alSourcei(sourceId, AL_SOURCE_RELATIVE, isGlobal ? 1 : 0);
    }

    /**
     * Sets loop state of the sound.
     * */
    public void setLoop(boolean state) {
        this.isLooping = state;
        if (sourceId != -1)
            alSourcei(sourceId, AL_LOOPING, isLooping ? 1 : 0);
    }
}
