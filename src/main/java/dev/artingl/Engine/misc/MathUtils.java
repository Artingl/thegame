package dev.artingl.Engine.misc;

import org.joml.Vector3f;

public class MathUtils {

    public static float easeInOutCirc(float x) {
        return (float)(x < 0.5f
                ? (1f - Math.sqrt(1f - Math.pow(2f * x, 2f))) / 2f
                : (Math.sqrt(1f - Math.pow(-2f * x + 2f, 2f)) + 1f) / 2f);
    }

    public static Vector3f spherical2cartesian(float radius, float pitch, float heading) {
        return new Vector3f(
                (float) (radius * Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(heading))),
                (float) (-radius * Math.sin(Math.toRadians(pitch))),
                (float) (radius * Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(heading)))
        );
    }

}
