package dev.artingl.Engine.models;

import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.texture.Texture;
import org.jetbrains.annotations.Nullable;

public interface BaseModel {

    /**
     * Load and/or parse the model.
     *
     * @param quality Quality of the model.
     * */
    VerticesBuffer load(MeshQuality quality);

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
     * */
    @Nullable
    Texture getTexture();
}
