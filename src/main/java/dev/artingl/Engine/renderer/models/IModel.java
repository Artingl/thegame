package dev.artingl.Engine.renderer.models;

import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;

public interface IModel {

    /**
     * Load and/or parse the model.
     *
     * @param quality  Quality of the model.
     * @param meshName The model to load
     */
    VerticesBuffer load(Quality quality, String meshName);

    /**
     * Set render mode to be used by default
     * */
    int getRenderMode();

    /**
     * Get model's resource.
     * */
    Resource getResource();

    /**
     * Get model's texture
     *
     * @param mesh Name of the mesh for which texture needs to be returned
     * */
    Texture getTexture(String mesh);

    /**
     * Cleanup the model
     * */
    void cleanup();

    /**
     * Get names of all meshes in the model
     * */
    String[] getMeshNames();

    /**
     * Get model's properties
     * */
    ModelProperties getProperties();
}
