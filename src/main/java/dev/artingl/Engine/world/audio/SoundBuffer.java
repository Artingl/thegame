package dev.artingl.Engine.world.audio;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.resources.Resource;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

public class SoundBuffer {

    private final Resource resource;
    private int bufferId;

    public SoundBuffer(Resource resource) {
        this.resource = resource;
        this.bufferId = -1;
    }

    public void init() throws IOException, EngineException {
        // Init buffer
        if (bufferId != -1)
            cleanup();
        this.bufferId = alGenBuffers();

        // Read PCM data from the resource
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            Engine.getInstance().getLogger().log(LogLevel.INFO, "Loading sound: channels=" + info.channels() + ", samples=" + info.sample_rate() + ", src=" + resource);
            ShortBuffer pcm = readVorbis(32 * 1024, info);
            alBufferData(bufferId, info.channels() > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, pcm, info.sample_rate());
            MemoryUtil.memFree(pcm);
        }
    }

    public void cleanup() {
        if (this.bufferId != -1) {
            alDeleteBuffers(this.bufferId);
            this.bufferId = -1;
        }
    }

    public int getBufferId() {
        return bufferId;
    }

    private ShortBuffer readVorbis(int bufferSize, STBVorbisInfo info) throws IOException, EngineException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vorbis = Utils.inputToByteBuffer(resource.load(), bufferSize);
            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == 0)
                throw new EngineException("Failed to open Ogg Vorbis file. Error: " + error.get(0));

            stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer pcm = MemoryUtil.memAllocShort(lengthSamples);
            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            stb_vorbis_close(decoder);

            return pcm;
        }
    }
}
