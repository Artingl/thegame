package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.misc.MathUtils;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class BaseMesh implements IMesh {
    public static final ShaderProgram BASE_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/base_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/base_mesh.frag"))
    );

    public static final ShaderProgram INSTANCED_BASE_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/instanced_base_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/base_mesh.frag"))
    );

    static {
        try {
            BASE_PROGRAM.bake();
            INSTANCED_BASE_PROGRAM.bake();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    private int verticesCount;
    private int indicesCount;
    private VerticesBuffer vertices;
    private Matrix4f modelMatrix;
    private int instancesVBO, vao, vbo, ebo;
    private float meshFade;
    private int mode;
    private boolean isBaked;
    private boolean isDirty;
    private ShaderProgram program;
    private final List<Matrix4f> instances;
    private boolean enableFadeAnimation;


    public BaseMesh() {
        this(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), null);
    }

    public BaseMesh(Vector3f position, Vector3f rotation, VerticesBuffer vertices) {
        this.vertices = vertices;
        this.vao = -1;
        this.instancesVBO = -1;
        this.vbo = -1;
        this.ebo = -1;
        this.verticesCount = -1;
        this.indicesCount = 1;
        this.isBaked = false;
        this.isDirty = true;
        this.program = BASE_PROGRAM;
        this.mode = GL_TRIANGLES;
        this.instances = new ArrayList<>();
        this.vertices = new VerticesBuffer();
        this.enableFadeAnimation = true;
    }

    /**
     * Toggle mesh fade animation after being baked
     * */
    public void toggleFade(boolean state) {
        this.enableFadeAnimation = state;
    }

    /**
     * Set render mode to be used by default
     * */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Set the mesh as dirty
     */
    @Override
    public void makeDirty() {
        isDirty = true;
    }

    /**
     * Set vertices in the mesh and set it as dirty.
     *
     * @param vertices Vertices to be set
     */
    public void setVertices(VerticesBuffer vertices) {
        synchronized (this.vertices) {
            if (this.vertices != null)
                this.vertices.cleanup();
            if (vertices != null)
                this.vertices = vertices;
            this.isDirty = true;
        }
    }

    /**
     * Set shader program that will be used on rendering of this mesh
     *
     * @param program The program to be set
     * */
    public void setShaderProgram(@Nullable ShaderProgram program) {
        this.program = program;
    }

    private void fadeAnimationStep() {
        // Update mesh fade state, so the mesh would appear smoothly after being baked
        // TODO: use the timer for delta calculation
        if (this.enableFadeAnimation) {
            float delta = 1f / Engine.getInstance().getProfiler().getFPS();
            this.meshFade = Math.min(1, this.meshFade + delta);
        }
        else this.meshFade = 1;
    }

    @Override
    @Nullable
    public ShaderProgram getShaderProgram() {
        return program;
    }


    @Override
    public void render(RenderContext context) {
        int mode = context.getRenderer().isWireframeEnabled() ? GL_LINES : this.mode;
        render(context, mode);
    }

    @Override
    public void render(RenderContext context, int mode) {
        if (!this.isBaked)
            return;
        this.fadeAnimationStep();

        // Use the shader program if we have one
        if (program != null) {
            program.use();
            program.updateModelMatrix(getModelMatrix());
            program.setUniformFloat("meshFade", MathUtils.easeInOutCirc(meshFade));

            Viewport viewport = context.getViewport();
            viewport.uploadMatrices(program);
        }

        // Render the mesh
        if (verticesCount != -1)
            context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, vao, mode, verticesCount);
        else if (indicesCount != -1)
            context.getRenderer().drawCall(Renderer.DrawCall.ELEMENTS, ebo, mode, indicesCount);
    }
    @Override
    public void renderInstanced(RenderContext context) {
        this.renderInstanced(context, GL_TRIANGLES);
    }

    @Override
    public void renderInstanced(RenderContext context, int mode) {
        if (!this.isBaked)
            return;
        this.fadeAnimationStep();

        // Use the shader program if we have one
        this.program = INSTANCED_BASE_PROGRAM;
        INSTANCED_BASE_PROGRAM.use();
        INSTANCED_BASE_PROGRAM.setUniformFloat("meshFade", MathUtils.easeInOutCirc(meshFade));
        Viewport viewport = context.getViewport();
        viewport.uploadMatrices(INSTANCED_BASE_PROGRAM);

        // Render the mesh
        if (verticesCount != -1)
            context.getRenderer().drawCallInstanced(Renderer.DrawCall.ARRAYS, vao, mode, verticesCount, this.instances.size());
        else if (indicesCount != -1)
            context.getRenderer().drawCallInstanced(Renderer.DrawCall.ELEMENTS, ebo, mode, indicesCount, this.instances.size());
    }

    @Override
    public void setQuality(MeshQuality quality) {
        // Does noting by default
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
        this.makeDirty();
    }

    @Override
    public void cleanup() {
        synchronized (this.vertices) {
            // Deactivate the mesh in the mesh manager
            Engine.getInstance()
                    .getRenderer()
                    .getMeshManager()
                    .deactivateMesh(this);

            if (this.vao != -1)
                glDeleteVertexArrays(vao);
            if (this.vbo != -1)
                glDeleteBuffers(vbo);
            if (this.ebo != -1)
                glDeleteBuffers(ebo);
            if (this.instancesVBO != -1)
                glDeleteBuffers(this.instancesVBO);
            this.vao = -1;
            this.vbo = -1;
            this.ebo = -1;
            this.instancesVBO = -1;
            this.verticesCount = -1;
            this.indicesCount = 1;

            if (this.vertices != null)
                this.vertices.cleanup();
            this.instances.clear();
        }
    }

    @Override
    public void bake() {
        synchronized (this.vertices) {
            this.meshFade = 0;

            if (this.isBaked && this.isDirty) {
                if (this.vao != -1)
                    glDeleteVertexArrays(vao);
                if (this.vbo != -1)
                    glDeleteBuffers(vbo);
                if (this.ebo != -1)
                    glDeleteBuffers(ebo);
                if (this.instancesVBO != -1)
                    glDeleteBuffers(this.instancesVBO);
            } else if (this.isBaked)
                return;

            this.ebo = -1;
            this.vao = -1;
            this.vbo = -1;
            this.instancesVBO = -1;
            this.verticesCount = -1;
            this.indicesCount = 1;

            if (this.vertices == null)
                return;

            if (!this.instances.isEmpty())
                this.instancesVBO = glGenBuffers();
            this.vao = glGenVertexArrays();
            this.vbo = glGenBuffers();

            if (this.vertices.hasIndices()) {
                this.ebo = glGenBuffers();
                this.indicesCount = this.vertices.bake(vao, vbo, ebo);
            } else
                this.verticesCount = this.vertices.bake(vao, vbo, ebo);

            // Initialize instance rendering if we have any instances provided
            if (!this.instances.isEmpty()) {
                float[] instances = new float[this.instances.size() * 16];
                int i = 0;
                for (Matrix4f mat : this.instances) {
                    mat.get(instances, i);
                    i += 16;
                }

                glBindBuffer(GL_ARRAY_BUFFER, this.instancesVBO);
                glBufferData(GL_ARRAY_BUFFER, instances, GL_STATIC_DRAW);
                glVertexAttribPointer(3, 4, GL_FLOAT, false, 16 * 4, 0);
                glVertexAttribPointer(4, 4, GL_FLOAT, false, 16 * 4, 16);
                glVertexAttribPointer(5, 4, GL_FLOAT, false, 16 * 4, 32);
                glVertexAttribPointer(6, 4, GL_FLOAT, false, 16 * 4, 48);
                glEnableVertexAttribArray(3);
                glEnableVertexAttribArray(4);
                glEnableVertexAttribArray(5);
                glEnableVertexAttribArray(6);
                glVertexAttribDivisor(3, 1);
                glVertexAttribDivisor(4, 1);
                glVertexAttribDivisor(5, 1);
                glVertexAttribDivisor(6, 1);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            }

            this.isBaked = true;
            this.isDirty = false;

            // Activate the mesh in the mesh manager
            Engine.getInstance()
                    .getRenderer()
                    .getMeshManager()
                    .activateMesh(this);
        }
    }

    @Override
    public MeshQuality getQuality() {
        return MeshQuality.HIGH;
    }

    public VerticesBuffer getVerticesBuffer() {
        return vertices;
    }

    @Override
    public Matrix4f getModelMatrix() {
        return this.modelMatrix;
    }

    @Override
    public void transform(Matrix4f model) {
        this.modelMatrix = model;
    }

    @Override
    public int getVerticesCount() {
        return verticesCount;
    }

    @Override
    public int getIndicesCount() {
        return indicesCount;
    }

    @Override
    public boolean isBaked() {
        return this.isBaked;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    public int getVao() {
        return vao;
    }

    public int getEbo() {
        return ebo;
    }

    public int getVbo() {
        return vbo;
    }

}
