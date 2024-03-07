package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.scene.VertexBuffer;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import org.joml.Vector3f;

import java.util.List;

public class MeshColliderComponent extends BaseColliderComponent {

    private final IMesh mesh;
    private MeshCollisionShape shape;

    public MeshColliderComponent(IMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected CollisionShape buildCollider() {
        if (shape == null && mesh.isBaked()) {
            VerticesBuffer[] buffers = mesh.getBuffer();
            int size = 0, offset = 0;

            // Calculate the buffer size for all vertices in all buffers
            for (VerticesBuffer buffer: buffers) {
                VerticesBuffer.Attribute[] attributes = buffer.getAttributes();
                List<Object> fields = buffer.getFields();
                size += (fields.size() / attributes.length) * 3;
            }

            // Iterate through all buffers which are used in the mesh and build the vertices buffer
            float[] finalVertices = new float[size];
            for (VerticesBuffer buffer: buffers) {
                VerticesBuffer.Attribute[] attributes = buffer.getAttributes();
                List<Object> fields = buffer.getFields();

                // Since we build our meshes not like the jbullet expects it to be,
                // we need to build new mesh only with positions (we expect the first 3 floats of each vertex to be the position of the vertex)
                int vertIdx = 0;

                // Parse all positions in the mesh
                for (int i = 0; i < fields.size(); i += attributes.length) {
                    Object value = fields.get(i);

                    assert value instanceof Vector3f;

                    finalVertices[offset + (vertIdx++)] = ((Vector3f) value).x;
                    finalVertices[offset + (vertIdx++)] = ((Vector3f) value).y;
                    finalVertices[offset + (vertIdx++)] = ((Vector3f) value).z;
                }

                offset += (fields.size() / attributes.length) * 3;
            }

            com.jme3.scene.Mesh mesh = new com.jme3.scene.Mesh();
            mesh.setBuffer(VertexBuffer.Type.Position, 3, finalVertices);

            this.shape = new MeshCollisionShape(mesh);
        }

        return shape;
    }

    @Override
    public String getName() {
        return "Mesh Collider";
    }

}
