package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.resources.texture.Texture;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SphereMesh extends BaseMesh {

    private final float radius;
    private boolean updateMesh;
    private MeshQuality currentQuality;

    public SphereMesh(Color color, Texture texture, float radius) {
        this.setTexture(texture);
        this.setColor(color);
        this.radius = radius;
        this.currentQuality = MeshQuality.HIGH;
        this.updateMesh = true;
    }

    public float getRadius() {
        return radius;
    }

    protected VerticesBuffer generateSphereVertices(float radius, int stackCount, int sectorCount) {
        // http://www.songho.ca/opengl/gl_sphere.html
        VerticesBuffer buffer = new VerticesBuffer(VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC3F, VerticesBuffer.Attribute.VEC2F);
        List<Pair<Vector3f, Vector2f>> vertices = new ArrayList<>();

        float sectorStep = (float) (2 * Math.PI / sectorCount);
        float stackStep = (float) (Math.PI / stackCount);

        for (int i = 0; i <= stackCount; ++i) {
            float stackAngle = (float) (Math.PI / 2 - i * stackStep);
            float xy = (float) (radius * Math.cos(stackAngle));
            float z = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= sectorCount; ++j) {
                float sectorAngle = j * sectorStep;

                vertices.add(Pair.of(
                        new Vector3f((float) (xy * Math.cos(sectorAngle)), (float) (xy * Math.sin(sectorAngle)), z),
                        new Vector2f((float) j / sectorCount, (float) i / stackCount)));
            }
        }

        for (int i = 0; i < stackCount; ++i) {
            int vi1 = i * (sectorCount + 1);
            int vi2 = (i + 1) * (sectorCount + 1);

            for (int j = 0; j < sectorCount; ++j, ++vi1, ++vi2) {
                Pair<Vector3f, Vector2f> v1 = vertices.get(vi1);
                Pair<Vector3f, Vector2f> v2 = vertices.get(vi2);
                Pair<Vector3f, Vector2f> v3 = vertices.get(vi1 + 1);
                Pair<Vector3f, Vector2f> v4 = vertices.get(vi2 + 1);

                if (i == 0) {
                    Vector3f normal = computeFaceNormal(v1.getLeft(), v2.getLeft(), v4.getLeft());
                    buffer.addAttribute(v1.getLeft()).addAttribute(normal).addAttribute(v1.getRight());
                    buffer.addAttribute(v2.getLeft()).addAttribute(normal).addAttribute(v2.getRight());
                    buffer.addAttribute(v4.getLeft()).addAttribute(normal).addAttribute(v4.getRight());
                }
                else if (i == (stackCount - 1)) {
                    Vector3f normal = computeFaceNormal(v1.getLeft(), v2.getLeft(), v3.getLeft());
                    buffer.addAttribute(v1.getLeft()).addAttribute(normal).addAttribute(v1.getRight());
                    buffer.addAttribute(v2.getLeft()).addAttribute(normal).addAttribute(v2.getRight());
                    buffer.addAttribute(v3.getLeft()).addAttribute(normal).addAttribute(v3.getRight());
                }
                else {
                    Vector3f normal = computeFaceNormal(v1.getLeft(), v2.getLeft(), v3.getLeft());
                    buffer.addAttribute(v1.getLeft()).addAttribute(normal).addAttribute(v1.getRight());
                    buffer.addAttribute(v2.getLeft()).addAttribute(normal).addAttribute(v2.getRight());
                    buffer.addAttribute(v3.getLeft()).addAttribute(normal).addAttribute(v3.getRight());

                    buffer.addAttribute(v3.getLeft()).addAttribute(normal).addAttribute(v3.getRight());
                    buffer.addAttribute(v2.getLeft()).addAttribute(normal).addAttribute(v2.getRight());
                    buffer.addAttribute(v4.getLeft()).addAttribute(normal).addAttribute(v4.getRight());
                }
            }
        }

        vertices.clear();
        return buffer;
    }

    protected Vector3f computeFaceNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f normal = new Vector3f();
        float nx, ny, nz;

        // find 2 edge vectors: v1-v2, v1-v3
        float ex1 = v2.x - v1.x;
        float ey1 = v2.y - v1.y;
        float ez1 = v2.z - v1.z;
        float ex2 = v3.x - v1.x;
        float ey2 = v3.y - v1.y;
        float ez2 = v3.z - v1.z;

        // cross product: e1 x e2
        nx = ey1 * ez2 - ez1 * ey2;
        ny = ez1 * ex2 - ex1 * ez2;
        nz = ex1 * ey2 - ey1 * ex2;

        // normalize only if the length is > 0
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0.000001f) {
            // normalize
            float lengthInv = 1.0f / length;
            normal.x = nx * lengthInv;
            normal.y = ny * lengthInv;
            normal.z = nz * lengthInv;
        }

        return normal;
    }

    @Override
    public void bake() {
        if (this.updateMesh) {
            int stackCount = currentQuality == MeshQuality.HIGH ? 48 : currentQuality == MeshQuality.MEDIUM ? 28 : 7;
            int sectorCount = currentQuality == MeshQuality.HIGH ? 32 : currentQuality == MeshQuality.MEDIUM ? 25 : 12;
            this.setVertices(this.generateSphereVertices(radius, stackCount, sectorCount));
            this.updateMesh = false;
        }

        super.bake();
    }

//    @Override
//    public void setQuality(MeshQuality quality) {
//        if (quality != this.currentQuality) {
//            this.makeDirty();
//            this.updateMesh = true;
//        }
//
//        this.currentQuality = quality;
//    }
//
//    @Override
//    public MeshQuality getQuality() {
//        return currentQuality;
//    }
}
