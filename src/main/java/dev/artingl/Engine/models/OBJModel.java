package dev.artingl.Engine.models;

import com.mokiat.data.front.parser.*;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.mesh.MeshQuality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.texture.Texture;
import dev.artingl.Engine.texture.TextureManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;

public class OBJModel implements BaseModel {

    private final Resource objModel;
    private final List<String> objectsToLoad;
    private final VerticesBuffer[] buffers;
    private final IOBJParser parser;
    private final int mode;

    public OBJModel(Resource model, String... objects) {
        this(model, GL_TRIANGLES, objects);
    }

    public OBJModel(Resource model, int mode, String... objects) {
        this.mode = mode;
        this.parser = new OBJParser();
        this.buffers = new VerticesBuffer[MeshQuality.values().length-1];
        this.objectsToLoad = objects.length == 0 ? null : Arrays.asList(objects);
        this.objModel = model;
    }

    @Override
    public VerticesBuffer load(MeshQuality quality) {
        synchronized (buffers) {
            if (quality == MeshQuality.NOT_RENDERED)
                return VerticesBuffer.EMPTY;

            int bufferId = quality.ordinal();
            if (buffers[bufferId] != null)
                return buffers[bufferId].fork();

            Logger logger = Engine.getInstance().getLogger();
            VerticesBuffer buffer = new VerticesBuffer(
                    VerticesBuffer.Attribute.VEC3F,
                    VerticesBuffer.Attribute.VEC4F,
                    VerticesBuffer.Attribute.VEC2F
            );

            try {
                Resource resource = objModel.relative(quality.name().toLowerCase() + ".obj");
                com.mokiat.data.front.parser.OBJModel model = parser.parse(resource.load());
                this.parseModel(buffer, model);

                logger.log(LogLevel.INFO, "OBJ model " + resource +
                        "; quality=" + quality + " info: v=" + model.getVertices().size() +
                        ", n=" + model.getNormals().size() +
                        ", t=" + model.getTexCoords().size());
            } catch (IOException ex) {
                logger.exception(ex, "Unable to load OBJ model.");
                buffer.cleanup();
                buffer = null;
            }

            buffers[bufferId] = buffer;
            if (buffer != null)
                return buffer.fork();
            return null;
        }
    }

    private void parseModel(VerticesBuffer buffer, com.mokiat.data.front.parser.OBJModel model) {
        for (OBJObject object : model.getObjects()) {
            if (objectsToLoad != null)
                if (!objectsToLoad.contains(object.getName()))
                    continue;

            for (OBJMesh mesh : object.getMeshes()) {
                for (OBJFace face : mesh.getFaces()) {
                    for (OBJDataReference reference : face.getReferences()) {
                        OBJVertex vertex = model.getVertex(reference);
                        Vector3f position = new Vector3f(vertex.x, vertex.y, vertex.z);
                        Vector4f color = new Vector4f(1, 1, 1, 1);
                        Vector2f uv = new Vector2f();

                        if (reference.hasNormalIndex()) {
                            OBJNormal normal = model.getNormal(reference);
                            float clr = Math.min(1, 1 - (Math.abs(normal.x + normal.z) / 2));
                            color = new Vector4f(1 * clr, 1 * clr, 1 * clr, 1);
                        }
                        if (reference.hasTexCoordIndex()) {
                            OBJTexCoord texCoord = model.getTexCoord(reference);
                            uv.set(texCoord.u, texCoord.v);
                        }

                        buffer.addAttribute(position);
                        buffer.addAttribute(color);
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
        return objModel;
    }

    @Override
    public Texture getTexture() {
        TextureManager textureManager = Engine.getInstance().getTextureManager();
        return textureManager.getTexture(objModel);
    }
}
