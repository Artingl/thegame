package dev.artingl.Engine.renderer.viewport;

import dev.artingl.Engine.misc.Color;
import org.joml.Vector3f;

public interface IViewport {

    Vector3f getPosition();
    Vector3f getRotation();
    Vector3f getScale();

    Vector3f getProjectionRotation();

    float getFov();
    float getAspect();
    float getWidth();
    float getHeight();
    float getZNear();
    float getZFar();
    float getSize();

    Type getType();
    Color getBackgroundColor();
    boolean usePostprocessing();

    enum Type {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }

}
