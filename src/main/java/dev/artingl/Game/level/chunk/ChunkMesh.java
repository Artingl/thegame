package dev.artingl.Game.level.chunk;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.level.Level;

public class ChunkMesh extends BaseMesh {
    public static final ShaderProgram CHUNK_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/world/chunk_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/world/chunk_mesh.frag"))
    );

    private final Level level;
    private final Chunk chunk;
    private MeshQuality currentQuality;
    private final VerticesBuffer[] quality;

    public ChunkMesh(Chunk chunk, Level level) {
        this.level = level;
        this.chunk = chunk;
        this.quality = new VerticesBuffer[MeshQuality.values().length - 1];
        this.currentQuality = MeshQuality.NOT_RENDERED;
        this.setShaderProgram(CHUNK_PROGRAM);
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void bake() {
        if (this.currentQuality != MeshQuality.NOT_RENDERED) {
            if (this.quality[this.currentQuality.ordinal()] == null)
                return;
            this.setVertices(this.quality[this.currentQuality.ordinal()].fork());
        }

        super.bake();
    }

    @Override
    public void render(RenderContext context, int mode) {
        if (!CHUNK_PROGRAM.isBaked())
            CHUNK_PROGRAM.bake();

        if (this.isDirty() || !this.isBaked())
            return;

        // Send all necessary info to the shader
        CHUNK_PROGRAM.updateModelMatrix(getModelMatrix());
        CHUNK_PROGRAM.setUniformFloat("lightLevel", level.getLightLevel());
        CHUNK_PROGRAM.setUniformVector3f("skyColor", level.getSky().getColor().asVector3f());
        CHUNK_PROGRAM.setUniformVector3f("cameraPosition", level.getPlayerPosition());
        CHUNK_PROGRAM.use();

        Viewport viewport = context.getViewport();
        viewport.uploadMatrices(CHUNK_PROGRAM);

        // Render the mesh
        context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, getVao(), mode, getVerticesCount());
    }

    @Override
    public void setQuality(MeshQuality quality) {
        // Change current buffer if the quality of the chunk has changed
        if (currentQuality != quality) {
            this.makeDirty();
        }
        this.currentQuality = quality;
    }

    @Override
    public MeshQuality getQuality() {
        return this.currentQuality;
    }

    public void setVerticesQuality(MeshQuality quality, VerticesBuffer buffer) {
        this.quality[quality.ordinal()] = buffer;
    }
}
