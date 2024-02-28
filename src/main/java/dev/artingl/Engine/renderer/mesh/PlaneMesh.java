package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.resources.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PlaneMesh extends BaseMesh {

    private final float width, height;

    public PlaneMesh(float width, float height, Texture texture) {
        float halfw = width / 2f;
        float halfh = height / 2f;

        this.width = width;
        this.height = height;

        this.setVertices(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(-halfw, 0.0f,-halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f( halfw, 0.0f,-halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-halfw, 0.0f, halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f( halfw, 0.0f, halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f( halfw, 0.0f,-halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-halfw, 0.0f, halfh)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 1))
        );
        this.setTexture(texture);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
