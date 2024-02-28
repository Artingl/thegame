package dev.artingl.Game.level.ambient;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Game.level.Level;

public class SkyMesh extends SphereMesh {

    public static final ShaderProgram SKY_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("thegame", "shaders/world/sky.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("thegame", "shaders/world/sky.frag"))
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
        super(Color.WHITE, Texture.MISSING, 300);
        this.level = level;
        this.sky = sky;
        this.color = Color.BLACK;
        this.setShaderProgram(SKY_PROGRAM);
    }

    public Sky getSky() {
        return sky;
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
        SKY_PROGRAM.updateModelMatrix(getModelMatrix());
        SKY_PROGRAM.setUniformFloat("currentRadius", getRadius());
        SKY_PROGRAM.setUniformFloat("lightLevel", level.getLightLevel());
        SKY_PROGRAM.setUniformVector3f("skyColor", color.asVector3f());
        SKY_PROGRAM.use();

        Viewport viewport = context.getViewport();
        IViewport iViewport = viewport.getCurrentViewport();
        viewport.uploadMatrices(SKY_PROGRAM);

        // Update the sky mesh if render distance has changed
        if (iViewport != null) {
            int rd = (int) (iViewport.getFarPlane() * 0.8f);
            if (this.lastRadius != rd) {
                this.lastRadius = rd;
                this.setVertices(this.generateSphereVertices(this.lastRadius, 6, 9));
                return;
            }
        }

        // Render the mesh
        context.getRenderer().drawCall(Renderer.DrawCall.ARRAYS, getVao(), mode, getVerticesCount());
    }
}
