package dev.artingl.Game.level.ambient;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.MathUtils;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.Chunk;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.GL_LINES;

public class SkyMesh extends SphereMesh {

    public static final ShaderProgram SKY_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/sky.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/sky.frag"))
    );

    static {
        try {
            SKY_PROGRAM.bake();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    private Color color;


    private final Level level;
    private final Sky sky;
    private int lastRadius = -1;

    public SkyMesh(Sky sky, Level level) {
        super(300);
        this.level = level;
        this.sky = sky;
        this.color = Color.BLACK;
        this.setShaderProgram(SKY_PROGRAM);
    }

    @Override
    public VerticesBuffer generateSphereVertices(float rows, float cols, float radius) {
        VerticesBuffer verticesBuffer = new VerticesBuffer(VerticesBuffer.Attribute.VEC3F);

        float pitchAngle = 360.0f / rows;
        float headingAngle = 360.0f / cols;

        for (float pitch = 00.0f + pitchAngle; pitch < 360.0f; pitch += pitchAngle) {
            for (float heading = 0.0f; heading < 360.0f; heading += headingAngle) {
                Vector3f pos0 = MathUtils.spherical2cartesian(radius, pitch, heading);
                Vector3f pos1 = MathUtils.spherical2cartesian(radius, pitch, heading + headingAngle);
                Vector3f pos2 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading);
                Vector3f pos3 = MathUtils.spherical2cartesian(radius, pitch + pitchAngle, heading + headingAngle);

                verticesBuffer
                        .addAttribute(pos0)
                        .addAttribute(pos2)
                        .addAttribute(pos1)

                        .addAttribute(pos1)
                        .addAttribute(pos2)
                        .addAttribute(pos3);
            }
        }

        return verticesBuffer;
    }

    /**
     * Change sky's color
     * */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get current sky's color
     * */
    public Color getColor() {
        return color;
    }

    @Override
    public void render(RenderContext context, int mode) {
        // Send all necessary info to the shader
        SKY_PROGRAM.use();
        SKY_PROGRAM.updateModelMatrix(getModelMatrix());
        SKY_PROGRAM.setUniformFloat("currentRadius", getRadius());
        SKY_PROGRAM.setUniformFloat("lightLevel", level.getLightLevel());
        SKY_PROGRAM.setUniformVector3f("skyColor", color.asVector3f());

        Viewport viewport = context.getViewport();
        IViewport iViewport = viewport.getCurrentViewport();
        viewport.uploadMatrices(SKY_PROGRAM);

        // Update the sky mesh if render distance has changed
        if (iViewport != null) {
            int rd = (int) (iViewport.getZFar() * 0.8f);
            if (this.lastRadius != rd) {
                this.lastRadius = rd;
                this.setVertices(this.generateSphereVertices(32, 32, this.lastRadius));
                return;
            }
        }

        // Render the mesh
        context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, getVao(), mode, getVerticesCount());
    }
}
