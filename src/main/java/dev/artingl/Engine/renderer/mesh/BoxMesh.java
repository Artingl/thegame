package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.resources.texture.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BoxMesh extends BaseMesh {

    public BoxMesh() {
        this(new Vector3f(1, 1, 1));
    }

    public BoxMesh(Vector3f lengths) {
        this(lengths, Color.WHITE, Texture.MISSING);
    }

    public BoxMesh(Vector3f lengths, Color color) {
        this(lengths, color, Texture.MISSING);
    }

    public BoxMesh(Vector3f lengths, Texture texture) {
        this(lengths, Color.WHITE, texture);
    }

    public BoxMesh(Vector3f lengths, Color color, Texture texture) {
        float lx = lengths.x*0.5f, ly = lengths.y*0.5f, lz = lengths.z*0.5f;
        this.setVertices(
                new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC2F)
                        .addAttribute(new Vector3f(-lx, -ly, -lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(-lx, -ly, lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, -ly, -lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(lx, -ly, lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(lx, -ly, -lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, -ly, lz)).addAttribute(new Vector3f(0, -1, 0)).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f(-lx, ly, -lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(-lx, ly, lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, ly, -lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(lx, ly, lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(lx, ly, -lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, ly, lz)).addAttribute(new Vector3f(0, 1, 0)).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f(-lx, -ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(lx, -ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-lx, ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, -ly, -lz)).addAttribute(new Vector3f(0, 0, -1)).addAttribute(new Vector2f(1, 0))

                        .addAttribute(new Vector3f(-lx, -ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(lx, -ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-lx, ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, -ly, lz)).addAttribute(new Vector3f(0, 0, 1)).addAttribute(new Vector2f(1, 0))

                        .addAttribute(new Vector3f(-lx, -ly, -lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(-lx, -ly, lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(-lx, ly, -lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, ly, lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(-lx, ly, -lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(-lx, -ly, lz)).addAttribute(new Vector3f(-1, 0, 0)).addAttribute(new Vector2f(0, 1))

                        .addAttribute(new Vector3f(lx, -ly, -lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(0, 0))
                        .addAttribute(new Vector3f(lx, -ly, lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(0, 1))
                        .addAttribute(new Vector3f(lx, ly, -lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(lx, ly, lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(1, 1))
                        .addAttribute(new Vector3f(lx, ly, -lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(1, 0))
                        .addAttribute(new Vector3f(lx, -ly, lz)).addAttribute(new Vector3f(1, 0, 0)).addAttribute(new Vector2f(0, 1))
        );
        this.setColor(color);
        this.setTexture(texture);
    }

}
