package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.resources.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

public class SquareMesh extends BaseMesh {

    public SquareMesh(float width, float height) {
        this(width, height, Color.WHITE, Texture.MISSING);
    }

    public SquareMesh(float width, float height, Color color) {
        this(width, height, color, Texture.MISSING);
    }

    public SquareMesh(float width, float height, Texture texture) {
        this(width, height, Color.WHITE, texture);
    }

    public SquareMesh(float width, float height, Color color, Texture texture) {
        super(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC4F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(-(0.5f * width), -(0.5f * height), 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(0.5f * width, -(0.5f * height), 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-(0.5f * width), 0.5f * height, 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f(0.5f * width, 0.5f * height, 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-(0.5f * width), 0.5f * height, 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(0.5f * width, -(0.5f * height), 0)).addAttribute(color.asVector4f()).addAttribute(new Vector2f(1, 0))
        );
        this.setTexture(texture);
        this.setColor(color);
    }

}
