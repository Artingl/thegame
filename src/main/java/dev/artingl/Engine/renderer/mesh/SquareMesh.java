package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

public class SquareMesh extends BaseMesh {

    public SquareMesh(float width, float height, Texture texture) {
        this.setVertices(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC4F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(0.0f, 0.0f, 0.0f)).addAttribute(new Vector4f(1, 1, 1, 1)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(1.0f + width, 0.0f, 0.0f)).addAttribute(new Vector4f(1, 1, 1, 1)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(1.0f + width, 1.0f + height, 0.0f)).addAttribute(new Vector4f(1, 1, 1, 1)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(0.0f, 1.0f + height, 0.0f)).addAttribute(new Vector4f(1, 1, 1, 1)).addAttribute(new Vector2f(0, 0))
        );
        this.setMode(GL_QUADS);
    }

}
