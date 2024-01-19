package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.misc.MathUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class CapsuleMesh extends BaseMesh {

    private final float radius;
    private final float height;

    public CapsuleMesh(float radius, float height) {
        this.setVertices(this.generateCapsuleVertices(32, 32, radius, height));
        this.radius = radius;
        this.height = height;
    }

    public float getHeight() {
        return height;
    }

    public float getRadius() {
        return radius;
    }

    public VerticesBuffer generateCapsuleVertices(float rows, float cols, float radius, float height) {
        VerticesBuffer verticesBuffer = new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC4F);

        float pitchAngle = 360.0f / rows;
        float headingAngle = 360.0f / cols;
        float yOffset;

        for (float pitch = 0.0f + pitchAngle; pitch < 180.0f; pitch += pitchAngle) {
            for (float heading = 0.0f; heading < 360.0f; heading += headingAngle) {
                Vector3f pos0 = MathUtils.spherical2cartesian(radius, pitch, heading);
                Vector3f pos1 = MathUtils.spherical2cartesian(radius, pitch, heading + headingAngle);
                Vector3f pos2 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading);
                Vector3f pos3 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading + headingAngle);

                verticesBuffer
                        .addAttribute(pos0).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1))

                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos3).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1));
            }
        }

        for (yOffset = 0; yOffset < height; yOffset += height / rows) {
            for (float heading = 0.0f; heading < 360.0f; heading += headingAngle) {
                Vector3f pos0 = MathUtils.spherical2cartesian(radius, 180, heading);
                Vector3f pos1 = MathUtils.spherical2cartesian(radius, 180, heading + headingAngle);
                Vector3f pos2 = MathUtils.spherical2cartesian(radius, 180 + pitchAngle, heading);
                Vector3f pos3 = MathUtils.spherical2cartesian(radius, 180 + pitchAngle, heading + headingAngle);

                pos0.y += yOffset;
                pos1.y += yOffset;
                pos2.y += yOffset;
                pos3.y += yOffset;

                verticesBuffer
                        .addAttribute(pos0).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1))

                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos3).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1));
            }
        }

        for (float pitch = 180.0f + pitchAngle; pitch < 360.0f; pitch += pitchAngle) {
            for (float heading = 0.0f; heading < 360.0f; heading += headingAngle) {
                Vector3f pos0 = MathUtils.spherical2cartesian(radius, pitch, heading);
                Vector3f pos1 = MathUtils.spherical2cartesian(radius, pitch, heading + headingAngle);
                Vector3f pos2 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading);
                Vector3f pos3 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading + headingAngle);

                pos0.y += yOffset;
                pos1.y += yOffset;
                pos2.y += yOffset;
                pos3.y += yOffset;

                verticesBuffer
                        .addAttribute(pos0).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1))

                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos3).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1));
            }
        }

        return verticesBuffer;
    }



}
