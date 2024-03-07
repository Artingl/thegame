package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import org.joml.Matrix4f;


public interface IMesh {

    /**
     * Render the mesh
     * Note: default mode is GL_TRIANGLES
     *
     * @param renderer Render context
     */
    void render(Renderer renderer);

    /**
     * Render the mesh with a mode
     *
     * @param renderer Render context
     * @param mode     Render mode (GL_TRIANGLES, GL_LINES, etc.)
     */
    void render(Renderer renderer, int mode);

    /**
     * Do instance rendering with the mesh
     * Note: default mode is GL_TRIANGLES;
     *
     * @param renderer Render context
     */
    void renderInstanced(Renderer renderer);

    /**
     * Do instance rendering with the mesh
     *
     * @param renderer Render context
     * @param mode     Render mode (GL_TRIANGLES, GL_LINES, etc.)
     */
    void renderInstanced(Renderer renderer, int mode);

    /**
     * Change mesh quality (amount of vertices drawn).
     * Note: this will make the mesh dirty if the quality is different from current.
     * */
    void setQuality(Quality quality);

    /**
     * Release resources occupied by the mesh
     * */
    void cleanup();

    /**
     * Bake the mesh
     * */
    void bake();

    /**
     * Get current mesh quality
     * */
    Quality getQuality();

    /**
     * Get the model matrix of the mesh
     * */
    Matrix4f getModelMatrix();

    /**
     * Was the mesh baked
     * */
    boolean isBaked();

    /**
     * Is the mesh dirty
     * */
    boolean isDirty();

    /**
     * Get shader program assigned to the mesh
     * */
    ShaderProgram getShaderProgram();

    /**
     * Get shader program used for instanced rendering assigned to the mesh
     * */
    ShaderProgram getInstancedShaderProgram();

    /**
     * Make the mesh dirty
     * */
    void makeDirty();

    /**
     * Change the mesh's model
     * */
    void transform(Matrix4f model);

    /**
     * Amount of vertices to be drawn
     * */
    int getVerticesCount();

    /**
     * Amount of indices to be drawn.
     * Note: can be -1
     */
    int getIndicesCount();

    /**
     * Add instance to be rendered (for instanced rendering).
     * Note: this will make the mesh dirty.
     *
     * @param buffer Buffer with the values of the instance.
     * */
    void addInstance(VerticesBuffer buffer);

    /**
     * Clear list of instances (for instanced rendering).
     * Note: this will make the mesh dirty.
     * */
    void clearInstances();

    /**
     * Completely reloads the mesh (useful for meshes loading its data from external sources, e.g. files).
     * Also makes the mesh dirty.
     * <p>
     * Note: avoid calling this method and rather use the Engine::reload, since it'd do the full reload without issues
     * */
    void reload();

    /**
     * Get all buffers which are used to build the mesh.
     */
    VerticesBuffer[] getBuffer();



    /**
     * Set shader program that will be used for rendering of this mesh
     *
     * @param program The program to be set
     * */
    void setShaderProgram(ShaderProgram program);

    /**
     * Set shader program that will be used for instanced rendering of this mesh
     *
     * @param program The program to be set
     * */
    void setInstancedShaderProgram(ShaderProgram program);

    /**
     * Set mesh's opacity
     * */
    void setOpacity(float opacity);
}
