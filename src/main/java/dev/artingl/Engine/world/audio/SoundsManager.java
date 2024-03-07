package dev.artingl.Engine.world.audio;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.world.Dimension;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.renderer.viewport.ViewportManager;
import dev.artingl.Engine.world.scene.BaseScene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public class SoundsManager {

    public final Logger logger;
    public final Engine engine;
    private final ConcurrentLinkedDeque<SoundSource> sources;
    private long device;
    private long context;

    public SoundsManager(Logger logger, Engine engine) {
        this.logger = logger;
        this.engine = engine;
        this.sources = new ConcurrentLinkedDeque<>();
    }

    public void addSource(SoundSource source) {
        try {
            source.init();
            source.play();
            this.sources.add(source);
        } catch (Exception e) {
            this.logger.exception(e, "Unable to initialize sound source!");
        }
    }

    public void removeSource(SoundSource source) {
        if (this.sources.remove(source))
            source.cleanup();
    }

    public void setGlobalVolume(float value) {
        alListenerf(AL_GAIN, value);
    }

    public void frame() {
        ViewportManager viewport = engine.getRenderer().getViewport();
        Viewport iviewport = viewport.getCurrentViewport();

        // Check if we have any active viewports to which we can play the sound
        if (iviewport != null) {
            Vector3f position = iviewport.getPosition();
            alListener3f(AL_POSITION, position.x, position.y, position.z);
            alListener3f(AL_VELOCITY, 0, 0, 0);
            alListenerfv(AL_ORIENTATION, makeOrientation(viewport.getView()));

            // Check if we have any scene set, otherwise don't play sound at all
            BaseScene currentScene = engine.getSceneManager().getCurrentScene();
            if (currentScene == null)
                return;

            Dimension currentDimension = currentScene.getDimension();

            // Stop sounds which are too far away and play those which are close
            for (SoundSource source : sources) {
                float cameraDst = position.distance(source.getPosition());

                // Stop the source if the current dimension is not the same as the one set in the source
                if (!source.getDimension().equals(currentDimension) && source.isPlaying()) {
                    source.stop();
                    continue;
                }

                // TODO: be able to control the distance in settings
                // TODO: this if statement loops the sound
//                if (!source.isPlaying() && cameraDst < 250)
//                    source.play();
//                else if (source.isPlaying() && cameraDst > 250)
//                    source.stop();
            }
        }
    }

    public void init() throws EngineException {
        this.logger.log(LogLevel.INFO, "Initializing sound subsystem.");
        this.device = alcOpenDevice((ByteBuffer) null);
        if (this.device == 0)
            throw new EngineException("Failed to initialize OpenAL device.");
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = alcCreateContext(device, (IntBuffer) null);
        if (context == 0)
            throw new EngineException("Failed to create OpenAL context.");
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        alDistanceModel(AL11.AL_EXPONENT_DISTANCE_CLAMPED);
        alListener3f(AL_POSITION, Integer.MAX_VALUE, 0, 0);
    }

    public void terminate() {
        this.sources.clear();
    }

    private float[] makeOrientation(Matrix4f mat) {
        mat = mat.invert();
        Vector3f at = new Vector3f(0, 0, -1);
        mat.transformDirection(at);
        Vector3f up = new Vector3f(0, 1, 0);
        mat.transformDirection(up);
        float[] data = new float[6];
        data[0] = at.x;
        data[1] = at.y;
        data[2] = at.z;
        data[3] = up.x;
        data[4] = up.y;
        data[5] = up.z;
        return data;
    }
}
