package dev.artingl.Game.level.chunk;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.level.Level;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

public class ChunkMesh extends BaseMesh {
    public static final ShaderProgram CHUNK_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/chunk_mesh.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/chunk_mesh.frag"))
    );

    static {
        try {
            CHUNK_PROGRAM.bake();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    private final Level level;
    private final Chunk chunk;

    public ChunkMesh(Chunk chunk, Level level) {
        this.level = level;
        this.chunk = chunk;
        this.setShaderProgram(CHUNK_PROGRAM);
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void render(RenderContext context, int mode) {
        if (!this.isBaked())
            this.bake();

        // Send all necessary info to the shader
        CHUNK_PROGRAM.use();
        CHUNK_PROGRAM.updateModelMatrix(getModelMatrix());
        CHUNK_PROGRAM.setUniformFloat("lightLevel", level.getLightLevel());
        CHUNK_PROGRAM.setUniformVector3f("skyColor", level.getSky().getColor().asVector3f());
        CHUNK_PROGRAM.setUniformVector3f("cameraPosition", level.getPlayerPosition());

        Viewport viewport = context.getViewport();
        viewport.uploadMatrices(CHUNK_PROGRAM);

        // Render the mesh
        context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, getVao(), mode, getVerticesCount());
    }
}
