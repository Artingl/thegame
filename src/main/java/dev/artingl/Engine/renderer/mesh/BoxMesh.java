package dev.artingl.Engine.renderer.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BoxMesh extends BaseMesh {

    public BoxMesh(Vector3f lengths) {
        // TODO: calculate normals
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
    }

}
