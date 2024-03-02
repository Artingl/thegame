package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.MathUtils;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class BaseMesh implements IMesh {
    public static final ShaderProgram BASE_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/mesh/base_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/mesh/base_mesh.frag"))
    );

    public static final ShaderProgram INSTANCED_BASE_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/mesh/instanced_base_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/mesh/base_mesh.frag"))
    );

    private int verticesCount;
    private int indicesCount;
    private VerticesBuffer vertices;
    private Matrix4f modelMatrix;
    private int instancesVBO, vao, vbo, ebo;
    private float meshFade;
    private float opacity;
    private int mode;
    private boolean isBaked;
    private boolean isDirty;
    private Texture currentTexture;
    private ShaderProgram program;
    private ShaderProgram instancedProgram;
    private final List<VerticesBuffer> instances;
    private boolean enableFadeAnimation;
    private Color color;


    public BaseMesh() {
        this(null);
    }

    public BaseMesh(VerticesBuffer vertices) {
        this.vertices = vertices;
        this.vao = -1;
        this.instancesVBO = -1;
        this.vbo = -1;
        this.ebo = -1;
        this.verticesCount = -1;
        this.indicesCount = 1;
        this.color = Color.WHITE;
        this.opacity = 1;
        this.isBaked = false;
        this.isDirty = true;
        this.program = BASE_PROGRAM;
        this.instancedProgram = INSTANCED_BASE_PROGRAM;
        this.mode = GL_TRIANGLES;
        this.instances = new ArrayList<>();
        this.currentTexture = Texture.MISSING;
        this.enableFadeAnimation = true;

        if (this.vertices == null)
            this.vertices = new VerticesBuffer();
    }

    /**
     * Set mesh's texture
     * */
    public void setTexture(Texture texture) {
        this.currentTexture = texture;
    }

    /**
     * Get current mesh's texture
     * */
    @Nullable
    public Texture getTexture() {
        return currentTexture;
    }

    /**
     * Set mesh's opacity
     * */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /**
     * Returns mesh's opacity
     * */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Toggle mesh fade animation after being baked
     * */
    public void toggleFade(boolean state) {
        this.enableFadeAnimation = state;
    }

    /**
     * Set mesh's color
     *
     * @param color Color to be set
     * */
    public void setColor(Color color) {
        this.color = color;
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

    public void setShaderProgram(ShaderProgram program) {
        this.program = program;
    }

    public void setInstancedShaderProgram(ShaderProgram program) {
        this.instancedProgram = program;
    }

    private void fadeAnimationStep() {
        // Update mesh fade state, so the mesh would appear smoothly after being baked
        // TODO: use the timer for delta calculation
        // TODO: make it as a transition between old and new mesh
        if (this.enableFadeAnimation) {
            float delta = 1f / Engine.getInstance().getProfiler().getFPS();
            this.meshFade = Math.min(1, this.meshFade + delta);
        }
        else this.meshFade = 1;
    }

    @Override
    public ShaderProgram getShaderProgram() {
        return program;
    }

    @Override
    public ShaderProgram getInstancedShaderProgram() {
        return instancedProgram;
    }


    @Override
    public void render(RenderContext context) {
        render(context, mode);
    }

    @Override
    public void render(RenderContext context, int mode) {
        if (!BASE_PROGRAM.isBaked() || !INSTANCED_BASE_PROGRAM.isBaked()) {
            BASE_PROGRAM.bake();
            INSTANCED_BASE_PROGRAM.bake();
        }

        if (verticesCount > 0 && vao <= 0) {
            Engine.getInstance().getLogger().log(LogLevel.WARNING, "Trying to render an empty mesh! VAO=%d, VERT_CNT=%d, INSTANCE=%s", vao, verticesCount, this);
            this.makeDirty();
            return;
        }
        if (indicesCount > 0 && ebo <= 0) {
            Engine.getInstance().getLogger().log(LogLevel.WARNING, "Trying to render an empty mesh! EBO=%d, INDT_CNT=%d, INSTANCE=%s", ebo, indicesCount, this);
            this.makeDirty();
            return;
        }

        if (!this.isBaked || mode <= 0)
            return;
        this.fadeAnimationStep();


        // Use the shader program if we have one
        if (program != null) {
            program.setMainTexture(currentTexture);
            program.updateModelMatrix(getModelMatrix());
            program.setUniformVector4f("color", color.asVector4f());
            program.setUniformFloat("opacity", this.opacity * MathUtils.easeInOutCirc(meshFade));
            Viewport viewport = context.getViewport();
            viewport.uploadMatrices(program);
            program.use();
        }

        // Render the mesh
        if (verticesCount > 0)
            context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, vao, mode, verticesCount);
        else if (indicesCount > 0)
            context.getRenderer().drawCall(Renderer.DrawCall.ELEMENTS, ebo, mode, indicesCount);
    }

    @Override
    public void renderInstanced(RenderContext context) {
        this.renderInstanced(context, mode);
    }

    @Override
    public void renderInstanced(RenderContext context, int mode) {
        if (!BASE_PROGRAM.isBaked() || !INSTANCED_BASE_PROGRAM.isBaked()) {
            BASE_PROGRAM.bake();
            INSTANCED_BASE_PROGRAM.bake();
        }

        if (verticesCount > 0 && vao <= 0) {
            Engine.getInstance().getLogger().log(LogLevel.WARNING, "Trying to render an empty mesh! VAO=%d, VERT_CNT=%d, INSTANCE=%s", vao, verticesCount, this);
            this.makeDirty();
            return;
        }
        if (indicesCount > 0 && ebo <= 0) {
            Engine.getInstance().getLogger().log(LogLevel.WARNING, "Trying to render an empty mesh! EBO=%d, INDT_CNT=%d, INSTANCE=%s", ebo, indicesCount, this);
            this.makeDirty();
            return;
        }

        if (!this.isBaked || this.instances.isEmpty() || mode <= 0)
            return;
        this.fadeAnimationStep();

        this.instancedProgram.setMainTexture(currentTexture);
        this.instancedProgram.updateModelMatrix(getModelMatrix());
        this.instancedProgram.setUniformVector4f("color", color.asVector4f());
        this.instancedProgram.setUniformFloat("opacity", this.opacity * MathUtils.easeInOutCirc(meshFade));
        this.instancedProgram.use();
        Viewport viewport = context.getViewport();
        viewport.uploadMatrices(this.instancedProgram);

        // Render the mesh
        if (verticesCount > 0)
            context.getRenderer().drawCallInstanced(Renderer.DrawCall.ARRAYS, vao, mode, verticesCount, this.instances.size());
        else if (indicesCount > 0)
            context.getRenderer().drawCallInstanced(Renderer.DrawCall.ELEMENTS, ebo, mode, indicesCount, this.instances.size());
    }

    @Override
    public void setQuality(MeshQuality quality) {
        this.makeDirty();
        // Does noting by default
    }

    @Override
    public void addInstance(VerticesBuffer buffer) {
        this.instances.add(buffer);
        this.makeDirty();
    }

    @Override
    public void clearInstances() {
        for (VerticesBuffer b: this.instances)
            b.cleanup();
        this.instances.clear();
        this.makeDirty();
    }

    @Override
    public void reload() {
        this.makeDirty();
    }

    @Override
    public VerticesBuffer[] getBuffer() {
        return new VerticesBuffer[]{vertices};
    }

    @Override
    public void cleanup() {
        synchronized (this.vertices) {
            // Deactivate the mesh in the mesh manager
            Engine.getInstance()
                    .getRenderer()
                    .getMeshManager()
                    .deactivateMesh(this);

            if (this.vao > 0)
                glDeleteVertexArrays(vao);
            if (this.vbo > 0)
                glDeleteBuffers(vbo);
            if (this.ebo > 0)
                glDeleteBuffers(ebo);
            if (this.instancesVBO > 0)
                glDeleteBuffers(this.instancesVBO);
            this.vao = -1;
            this.vbo = -1;
            this.ebo = -1;
            this.instancesVBO = -1;
            this.verticesCount = -1;
            this.indicesCount = -1;

            if (this.vertices != null)
                this.vertices.cleanup();
            this.instances.clear();
        }
    }

    @Override
    public void bake() {
        // Some classes which extend this BaseMesh class sometimes would want not to render the mesh.
        // In this case, skip the baking process if the mesh rendering is disabled
        if (getQuality() == MeshQuality.NOT_RENDERED)
            return;
        if (!this.isDirty && this.isBaked)
            return;

        synchronized (this.vertices) {
            if (this.vao > 0)
                glDeleteVertexArrays(vao);
            if (this.vbo > 0)
                glDeleteBuffers(vbo);
            if (this.ebo > 0)
                glDeleteBuffers(ebo);
            if (this.instancesVBO > 0)
                glDeleteBuffers(this.instancesVBO);

            this.meshFade = 0;
            this.ebo = -1;
            this.vao = -1;
            this.vbo = -1;
            this.instancesVBO = -1;
            this.verticesCount = -1;
            this.indicesCount = -1;

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
            if (!this.instances.isEmpty() && this.instancesVBO > 0) {
                int indexOffset = this.vertices.getAttributes().length;
                for (VerticesBuffer buffer: this.instances) {
                    buffer.bake(-1, this.instancesVBO, -1, indexOffset);
                }
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
