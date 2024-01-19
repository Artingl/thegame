package dev.artingl.Engine.misc.noise;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Random;

public class PerlinNoise {
    private final ImprovedNoise[] noiseLevels;
    private final int levels;

    public PerlinNoise(int levels, int seed) {
        this(levels, new Random(seed));
    }

    public PerlinNoise(int levels, Random random) {
        this.levels = levels;
        this.noiseLevels = new ImprovedNoise[levels];

        for(int i = 0; i < levels; ++i) {
            this.noiseLevels[i] = new ImprovedNoise(random);
        }
    }

    public double getValue(Vector3f vec) {
        return getValue(vec.x, vec.y, vec.z);
    }

    public double getValue(Vector2f vec) {
        return getValue(vec.x, vec.y);
    }

    public double getValue(double x, double y, double z) {
        double value = 0.0D;
        double pow = 1.0D;

        for(int i = 0; i < this.levels; ++i) {
            value += this.noiseLevels[i].getValue(x / pow, y / pow, z / pow) * pow;
            pow *= 3.0D;
        }

        return value;
    }

    public double getValue(double x, double y) {
        double value = 0.0D;
        double pow = 1.0D;

        for(int i = 0; i < this.levels; ++i) {
            value += this.noiseLevels[i].getValue(x / pow, y / pow) * pow;
            pow *= 2.0D;
        }

        return value;
    }

    public float getValue(Settings settings, float x, float y)
    {
        float maxAmp = 0;
        float amp = 1;
        float freq = settings.scale;
        float noise = 0;

        //add successively smaller, higher-frequency terms
        for(int i = 0; i < settings.iterations; ++i)
        {
            noise += (float) (getValue(x * freq, y * freq) * amp);
            maxAmp += amp;
            amp *= settings.persistence;
            freq *= 2;
        }

        //take the average value of the iterations
        noise /= maxAmp;

        //normalize the result
        noise = noise * (settings.high - settings.low) / 2 + (settings.high + settings.low) / 2;

        return noise;
    }

    public record Settings(int iterations, float persistence, float scale, float low, float high) {}

}