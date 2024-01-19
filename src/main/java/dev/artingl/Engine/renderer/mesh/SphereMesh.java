package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.misc.MathUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SphereMesh extends BaseMesh {

    private final float radius;

    public SphereMesh(float radius) {
        this.setVertices(this.generateSphereVertices(32, 32, radius));
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public VerticesBuffer generateSphereVertices(float rows, float cols, float radius) {
        VerticesBuffer verticesBuffer = new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC4F);

        float pitchAngle = 360.0f / rows;
        float headingAngle = 360.0f / cols;

        for (float pitch = 00.0f + pitchAngle; pitch < 360.0f; pitch += pitchAngle) {
            for (float heading = 0.0f; heading < 360.0f; heading += headingAngle) {
                Vector3f pos0 = MathUtils.spherical2cartesian(radius, pitch, heading);
                Vector3f pos1 = MathUtils.spherical2cartesian(radius, pitch, heading + headingAngle);
                Vector3f pos2 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading);
                Vector3f pos3 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading + headingAngle);

                verticesBuffer
                        .addAttribute(pos0).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos1).addAttribute(new Vector4f(1, 0, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 0, 1))

                        .addAttribute(pos1).addAttribute(new Vector4f(1, 1, 1, 1))
                        .addAttribute(pos3).addAttribute(new Vector4f(0, 1, 1, 1))
                        .addAttribute(pos2).addAttribute(new Vector4f(1, 1, 1, 1));
            }
        }

        return verticesBuffer;
    }


}
