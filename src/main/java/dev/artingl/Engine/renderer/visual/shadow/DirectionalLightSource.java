package dev.artingl.Engine.renderer.visual.shadow;

import org.joml.Vector3f;

public class DirectionalLightSource implements LightSource {

    private Vector3f rotation;
    private Vector3f position;

    public DirectionalLightSource(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }
}
