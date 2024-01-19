package dev.artingl.Engine.renderer.scene.components.collider;

import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.scene.components.MeshComponent;
import org.joml.Vector3f;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DTriMeshData;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.trimesh.DxTriMesh;

import java.util.List;

public class MeshColliderComponent extends BaseColliderComponent {

    private final int step;
    private DxTriMesh meshGeometry;

    public MeshColliderComponent() {
        this(1);
    }

    /**
     * Build the mesh collider using custom mesh data.
     * Note: vertices positions must be in XZY order
     *
     * @param vertices Mesh vertices
     * @param indices Mesh indices
     * */
    public void buildWithCustomMesh(float[] vertices, int[] indices) {
        DTriMeshData data = OdeHelper.createTriMeshData();
        data.build(vertices, indices);
        this.meshGeometry = (DxTriMesh) OdeHelper.createTriMesh(null, data, null, null, null);
        data.destroy();
        this.isColliderBuilt = true;
    }

    public MeshColliderComponent(int step) {
        this.step = step;
    }

    @Override
    public DGeom getGeometry() {
        return meshGeometry;
    }

    @Override
    protected void buildCollider() {
        // Load the node's mesh to the mesh collider
        MeshComponent component = getNode().getComponent(MeshComponent.class);
        if (component == null) {
            getEngine().getLogger().log(LogLevel.WARNING, "MeshComponent is null!");
            return;
        }

        BaseMesh mesh = (BaseMesh) component.mesh;
        VerticesBuffer buffer = mesh.getVerticesBuffer();

        // Do not build the mesh if it is dirty or not baked
        if (!mesh.isBaked() || mesh.isDirty())
            return;

        VerticesBuffer.Attribute[] attributes = buffer.getAttributes();
        List<Object> fields = buffer.getFields();

        // Since we build our meshes not like the odej4 expects it to be,
        // we need to build new mesh only with positions (should be first 3 floats of each vertex) here to be used later
        float[] vertices = new float[((fields.size() / attributes.length) * 3) / step];
        int[] indices = new int[(fields.size() / attributes.length) / step];
        int vertIdx = 0, indIdx = 0;

        // Parse all positions in the mesh
        for (int i = 0; i < fields.size(); i += attributes.length * step) {
            Object value = fields.get(i);

            assert value instanceof Vector3f;
            indices[indIdx++] += vertIdx / 3;

            // odej4 expects position to be in XZY form
            vertices[vertIdx++] = ((Vector3f) value).x;
            vertices[vertIdx++] = ((Vector3f) value).z;
            vertices[vertIdx++] = ((Vector3f) value).y;
        }

        if (this.meshGeometry != null)
            this.meshGeometry.destroy();

        this.buildWithCustomMesh(vertices, indices);
    }

    @Override
    public String getName() {
        return "Mesh Collider";
    }
}
