package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.renderer.models.IModel;
import dev.artingl.Engine.renderer.models.ModelProperties;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.resources.texture.Texture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;

public class ModelMesh implements IMesh {

    private final IModel model;

    // One model can have lots of meshes, so we'll store them in a map
    // where a key is the name of the mesh
    private final Map<String, BaseMesh> modelMeshes;

    // List of meshes to use
    private final List<String> meshes;

    private final List<Matrix4f> instances;
    private MeshQuality currentQuality;
    private Matrix4f modelMatrix;
    private int totalIndices, totalVertices;
    private boolean isDirty;
    private boolean isBaked;
    private boolean enableFadeAnimation;

    public ModelMesh(IModel model) {
        this(model, null);
    }

    /**
     * Mesh class for rendering models representing IModel interface (OBJModel, etc.)
     *
     * @param model  The model that will be used for rendering
     * @param meshes Names of the meshes of the model that would be rendered.
     *               If the value isnull, all meshes would be used
     * */
    public ModelMesh(IModel model, String ...meshes) {
        Engine engine = Engine.getInstance();

        this.model = model;
        this.modelMeshes = new ConcurrentHashMap<>();
        this.meshes = new ArrayList<>();
        this.instances = new ArrayList<>();
        this.modelMatrix = new Matrix4f();
        this.currentQuality = MeshQuality.HIGH;
        this.isDirty = true;
        this.enableFadeAnimation = true;

        // Render all meshes if the provided value is null
        if (meshes == null)
            meshes = model.getMeshNames();

        for (String mesh : meshes)
            this.addMesh(mesh);

        engine.getLogger().log(LogLevel.INFO, "Initializing model for rendering with " +
                this.meshes.size() + " meshes (" + String.join(", ", this.meshes) + ")");
    }

    /**
     * Toggle mesh fade animation after being baked
     * */
    public void toggleFade(boolean state) {
        this.enableFadeAnimation = state;
    }

    /**
     * Add mesh to the render list.
     * Note: makes the mesh dirty
     *
     * @param name The name of the mesh
     * */
    public void addMesh(String name) {
        this.meshes.add(name);
        this.modelMeshes.put(name, new BaseMesh());

        this.makeDirty();
    }

    /**
     * Remove mesh from the render list.
     * Note: makes the mesh dirty
     *
     * @param name The name of the mesh to remove
     * */
    public void removeMesh(String name) {
        this.meshes.remove(name);

        // Remove mesh from the map
        BaseMesh mesh;
        if ((mesh = this.modelMeshes.remove(name)) != null) {
            mesh.cleanup();
        }

        this.makeDirty();
    }

    /**
     * Get mesh by its name.
     *
     * @param name Mesh name
     * */
    public BaseMesh getMesh(String name) {
        return this.modelMeshes.get(name);
    }

    @Override
    public void render(RenderContext context) {
        render(context, GL_TRIANGLES);
    }

    private void setupRenderTexture(ShaderProgram program, BaseMesh mesh, String name) {
        Engine engine = Engine.getInstance();

        if (program != null) {
            Texture texture = model.getTexture(name);

            // Parse properties of the material, so we can set up texture properly
            ModelProperties.MaterialProperty material =
                    model.getProperties().getMaterialProperty(name);

            if (material != null) {
                if (material.getCustomTexture() != null)
                    texture = engine.getTextureManager().getTexture(material.getCustomTexture());

                texture.setTiling(material.isTextureTiled());
            }

            program.setMainTexture(texture);
        }
    }

    @Override
    public void render(RenderContext context, int mode) {
        int vert = 0, ind = 0;
        ShaderProgram program;

        // Render all meshes of the object that we should render
        for (String name: meshes) {
            BaseMesh mesh = this.modelMeshes.get(name);

            if (mesh == null) {
                // ???
                continue;
            }

            // Stop rendering if the mesh is dirty or not baked
            if (mesh.isDirty()) {
                this.isDirty = true;
                break;
            }
            else if (!mesh.isBaked()) {
                this.isBaked = false;
                break;
            }

            vert += mesh.getVerticesCount();
            ind += mesh.getIndicesCount();
            program = mesh.getShaderProgram();

            this.setupRenderTexture(program, mesh, name);
            mesh.toggleFade(this.enableFadeAnimation);
            mesh.transform(modelMatrix);
            mesh.render(context, mode);
        }

        this.totalVertices = vert;
        this.totalIndices = ind;
    }

    @Override
    public void renderInstanced(RenderContext context) {
        renderInstanced(context, GL_TRIANGLES);
    }

    @Override
    public void renderInstanced(RenderContext context, int mode) {
        int vert = 0, ind = 0;
        ShaderProgram program;

        // Render all meshes of the object that we should render
        for (String name: meshes) {
            BaseMesh mesh = this.modelMeshes.get(name);

            if (mesh == null) {
                // ???
                continue;
            }

            // Stop rendering if the mesh is dirty or not baked
            if (mesh.isDirty()) {
                this.isDirty = true;
                break;
            }
            else if (!mesh.isBaked()) {
                this.isBaked = false;
                break;
            }

            vert += mesh.getVerticesCount();
            ind += mesh.getIndicesCount();
            program = mesh.getShaderProgram();

            this.setupRenderTexture(program, mesh, name);
            mesh.toggleFade(this.enableFadeAnimation);
            mesh.transform(modelMatrix);
            mesh.renderInstanced(context, mode);
        }

        this.totalVertices = vert;
        this.totalIndices = ind;
    }

    @Override
    public void setQuality(MeshQuality quality) {
        if (this.currentQuality != quality)
            this.isDirty = true;
        this.currentQuality = quality;
    }

    @Override
    public void cleanup() {
        // Deactivate the mesh in the mesh manager
        Engine.getInstance()
                .getRenderer()
                .getMeshManager()
                .deactivateMesh(this);

        model.cleanup();

        // Cleanup all meshes
        for (BaseMesh mesh: this.modelMeshes.values()) {
            if (mesh != null)
                mesh.cleanup();
        }
    }

    @Override
    public void bake() {
        if (!this.isDirty && this.isBaked)
            return;

        // Bake all meshes
        for (Map.Entry<String, BaseMesh> entry: this.modelMeshes.entrySet()) {
            BaseMesh mesh = entry.getValue();
            String name = entry.getKey();

            mesh.setVertices(model.load(this.currentQuality, name));

            // Add all global instances for all meshes
            mesh.clearInstances();
            for (Matrix4f mat: this.instances) {
                mesh.addInstance(mat);
            }

            mesh.bake();
        }

        this.isDirty = false;
        this.isBaked = true;

        // Activate the mesh in the mesh manager
        Engine.getInstance()
                .getRenderer()
                .getMeshManager()
                .activateMesh(this);
    }

    @Override
    public MeshQuality getQuality() {
        return currentQuality;
    }

    @Override
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    @Override
    public boolean isBaked() {
        return this.isBaked;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public @Nullable ShaderProgram getShaderProgram() {
        return BaseMesh.BASE_PROGRAM;
    }

    @Override
    public void makeDirty() {
        this.isDirty = true;
    }

    @Override
    public void transform(Matrix4f model) {
        this.modelMatrix = model;
    }

    @Override
    public int getVerticesCount() {
        return this.totalVertices;
    }

    @Override
    public int getIndicesCount() {
        return this.totalIndices;
    }

    @Override
    public void addInstance(Matrix4f mat) {
        this.instances.add(mat);
        this.makeDirty();
    }

    @Override
    public void clearInstances() {
        this.instances.clear();
        this.makeDirty();
    }

    @Override
    public void reload() {
        this.model.cleanup();
        this.makeDirty();
    }

    @Override
    public VerticesBuffer getBuffer() {
        Engine.getInstance().getLogger().log(LogLevel.UNIMPLEMENTED, "ModelMesh cannot return VerticesBuffer in getBuffer method.");
        return null;
    }
}
