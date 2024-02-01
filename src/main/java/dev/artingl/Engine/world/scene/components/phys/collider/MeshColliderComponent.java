package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.scene.Spatial;
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
            VerticesBuffer buffer = mesh.getBuffer();
            VerticesBuffer.Attribute[] attributes = buffer.getAttributes();
            List<Object> fields = buffer.getFields();

            // Since we build our meshes not like the jbullet expects it to be,
            // we need to build new mesh only with positions (we expect the first 3 floats of each vertex to be the position of the vertex)
            float[] vertices = new float[(fields.size() / attributes.length) * 3];
            int vertIdx = 0;

            // Parse all positions in the mesh
            for (int i = 0; i < fields.size(); i += attributes.length) {
                Object value = fields.get(i);

                assert value instanceof Vector3f;

                vertices[vertIdx++] = ((Vector3f) value).x;
                vertices[vertIdx++] = ((Vector3f) value).y;
                vertices[vertIdx++] = ((Vector3f) value).z;
            }

            com.jme3.scene.Mesh mesh = new com.jme3.scene.Mesh();
            mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);

            this.shape = new MeshCollisionShape(mesh);
        }

        return shape;
    }

    @Override
    public String getName() {
        return "Terrain Collider";
    }

    public interface ITerrainHeightHandler {
        float run(float x, float z);
    }

}
