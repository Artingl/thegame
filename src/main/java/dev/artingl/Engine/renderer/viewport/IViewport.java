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
    float getNearPlane();
    float getFarPlane();
    float getSize();

    Type getType();
    Color getBackgroundColor();
    boolean usePostprocessing();

    enum Type {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }

}
