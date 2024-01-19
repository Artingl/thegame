package dev.artingl.Engine.renderer.mesh;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class PlaneMesh extends BaseMesh {

    private final float width, height;

    public PlaneMesh(float width, float height) {
        float halfw = width / 2f;
        float halfh = height / 2f;

        this.width = width;
        this.height = height;

        this.setVertices(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC4F)
                        .addAttribute(new Vector3f(-halfw, 0.0f,-halfh)).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(new Vector3f( halfw, 0.0f,-halfh)).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(new Vector3f(-halfw, 0.0f, halfh)).addAttribute(new Vector4f(1, 1, 1, 1))

                        .addAttribute(new Vector3f( halfw, 0.0f, halfh)).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(new Vector3f( halfw, 0.0f,-halfh)).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(new Vector3f(-halfw, 0.0f, halfh)).addAttribute(new Vector4f(1, 1, 1, 1))
        );
//        this.setMode(GL_QUADS);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public void bake() {
        super.bake();
    }

}
