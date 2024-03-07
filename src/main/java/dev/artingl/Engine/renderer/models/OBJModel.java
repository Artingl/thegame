package dev.artingl.Engine.renderer.models;

import com.mokiat.data.front.parser.*;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Engine.resources.texture.TextureManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;

public class OBJModel implements IModel {

    private final Resource resource;
    private final ModelProperties properties;

    // key is mesh name, buffer array represents quality levels
    private final Map<String, VerticesBuffer[]> meshes;

    // All available meshes in the model
    private String[] meshNames;

    private final IOBJParser parser;
    private final int mode;
    private boolean validMesh;

    public OBJModel(Resource model) {
        this(model, GL_TRIANGLES);
    }

    public OBJModel(Resource model, int mode) {
        this.mode = mode;
        this.parser = new OBJParser();
        this.resource = model;
        this.meshes = new ConcurrentHashMap<>();
        this.properties = new ModelProperties(this);
        this.validMesh = true;

        this.initializeModel();
    }

    @Override
    public VerticesBuffer load(Quality quality, String meshName) {
        Logger logger = Engine.getInstance().getLogger();
        VerticesBuffer[] buffers;

        if (!this.validMesh) {
            logger.log(LogLevel.WARNING, "Mesh " + meshName + " for model " + resource + " will not be loaded " +
                    "because the resource is invalid.");
            return VerticesBuffer.EMPTY;
        }

        // Check if the mesh name is valid
        if ((buffers = this.meshes.get(meshName)) == null) {
            logger.log(LogLevel.WARNING, "Invalid mesh " + meshName + " for model " + resource);
            return VerticesBuffer.EMPTY;
        }

        synchronized (meshes) {
            if (quality == Quality.NOT_RENDERED)
                return VerticesBuffer.EMPTY;

            int bufferId = quality.ordinal();
            if (buffers[bufferId] != null)
                return buffers[bufferId].fork();

            VerticesBuffer buffer = new VerticesBuffer(
                    VerticesBuffer.Attribute.VEC3F,
                    VerticesBuffer.Attribute.VEC3F,
                    VerticesBuffer.Attribute.VEC2F
            );

            try {
                Resource resource = this.resource.relative(quality.name().toLowerCase() + ".obj");
                com.mokiat.data.front.parser.OBJModel model = parser.parse(resource.load());
                this.parseModel(buffer, model, meshName);

                logger.log(LogLevel.INFO, "OBJ model " + resource +
                        "; quality=" + quality + " info: v=" + model.getVertices().size() +
                        ", n=" + model.getNormals().size() +
                        ", t=" + model.getTexCoords().size());
            } catch (IOException ex) {
                logger.exception(ex, "Unable to load OBJ model.");
                buffer.cleanup();
                buffer = null;
            }

            // Save buffers
            buffers[bufferId] = buffer;
            this.meshes.put(meshName, buffers);

            if (buffer != null)
                return buffer.fork();
            return null;
        }
    }

    private void parseModel(VerticesBuffer buffer, com.mokiat.data.front.parser.OBJModel model, String name) {
        for (OBJObject object : model.getObjects()) {
            if (!object.getName().equals(name))
                continue;

            for (OBJMesh mesh : object.getMeshes()) {
                for (OBJFace face : mesh.getFaces()) {
                    for (OBJDataReference reference : face.getReferences()) {
                        OBJVertex vertex = model.getVertex(reference);
                        Vector3f position = new Vector3f(vertex.x, vertex.y, vertex.z);
                        Vector3f normal = new Vector3f(0, 0, 0);
                        Vector2f uv = new Vector2f();

                        if (reference.hasNormalIndex()) {
                            OBJNormal verNormal = model.getNormal(reference);
                            normal.set(verNormal.x, verNormal.y, verNormal.z);
                        }
                        if (reference.hasTexCoordIndex()) {
                            OBJTexCoord texCoord = model.getTexCoord(reference);
                            uv.set(texCoord.u, texCoord.v);
                        }

                        buffer.addAttribute(position);
                        buffer.addAttribute(normal);
                        buffer.addAttribute(uv);
                    }
                }
            }
        }
    }

    @Override
    public int getRenderMode() {
        return mode;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public Texture getTexture(String mesh) {
        TextureManager textureManager = Engine.getInstance().getResourceManager().getTextureManager();
        return textureManager.getTexture(resource.relative(mesh));
    }

    @Override
    public void cleanup() {
        synchronized (meshes) {
            for (VerticesBuffer[] buffers: this.meshes.values()) {
                for (int i = 0; i < buffers.length; i++) {
                    if (buffers[i] != null)
                        buffers[i].cleanup();
                    buffers[i] = null;
                }
            }

            this.meshes.clear();
            this.initializeModel();
        }
    }

    @Override
    public String[] getMeshNames() {
        return this.meshNames;
    }

    @Override
    public ModelProperties getProperties() {
        return properties;
    }

    private void initializeModel() {
        Engine engine = Engine.getInstance();

        // Parse all mesh names
        try {
            Resource resource = this.resource.relative("high.obj");
            com.mokiat.data.front.parser.OBJModel objModel = parser.parse(resource.load());
            int i = 0;

            this.meshNames = new String[objModel.getObjects().size()];
            for (OBJObject object : objModel.getObjects()) {
                this.meshNames[i++] = object.getName();
            }
        } catch (Exception e) {
            engine.getLogger().exception(e, "Unable to load the model!");
            this.validMesh = false;
            return;
        }

        // Initialize meshes map with all possible names
        for (String name: this.meshNames) {
            this.meshes.put(name, new VerticesBuffer[Quality.values().length-1]);
        }
    }

    @Override
    public String toString() {
        return "OBJModel{resource=" + this.resource + "}";
    }
}
