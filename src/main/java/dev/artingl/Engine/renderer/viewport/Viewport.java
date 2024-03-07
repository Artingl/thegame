package dev.artingl.Engine.renderer.viewport;

import dev.artingl.Engine.misc.Color;
import org.joml.Vector3f;

public interface Viewport {

    Vector3f getPosition();
    Vector3f getRotation();
    Vector3f getScale();

    Vector3f getProjectionRotation();
    Vector3f getProjectionOffset();

    float getFov();
    float getAspect();
    float getWidth();
    float getHeight();
    float getNearPlane();
    float getFarPlane();
    float getSize();

    ViewType getViewType();
    RenderType getRenderType();
    Color getBackgroundColor();

    boolean isShadowMappingEnabled();
    boolean isPostprocessingEnabled();

    enum ViewType {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }

    enum RenderType {
        MAIN, UI
    }

}
