package dev.artingl.Engine.resources.texture;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public class TextureManager {

    private final Logger logger;
    private final Map<Resource, Texture> textures;
    private final List<Resource> allNamespaces;

    public TextureManager(Logger logger) {
        this.logger = logger;
        this.textures = new ConcurrentHashMap<>();
        this.allNamespaces = new ArrayList<>();
    }

    public void cleanup() {
        for (Texture texture: this.textures.values())
            texture.cleanup();
    }

    public void init() throws IOException {
    }

    /**
     * Load all textures from a resource
     *
     * @param resource Target resource
     */
    public void load(Resource resource) throws IOException, EngineException {
        this.allNamespaces.add(resource);
        this.loadNamespace(resource);
    }

    /**
     * Load texture to the main atlas
     *
     * @param target The resource from which the texture is going to be loaded
     *
     * @return The texture instance or null if the resource is invalid
     * */
    @Nullable
    public Texture loadTexture(Resource target) throws IOException {
        // Check that the file exists
        if (!target.exists())
            return null;

        BufferedImage texture = ImageIO.read(target.load());
        return new Texture(texture);
    }

    /**
     * Returns total amount of loaded textures
     * */
    public int totalTextures() {
        return this.textures.size();
    }

    public Texture getTexture(Resource texture) {
        if (!textures.containsKey(texture))
            return Texture.MISSING;

        return textures.get(texture);
    }

    private void loadNamespace(Resource resource) throws EngineException, IOException {
        // TODO: Use this to find textures inside a compiled jar

        // Get all texture files from resource
        URI uri = resource.getURI();
        if (uri == null)
            throw new EngineException("Unable to load textures: invalid resource " + resource);

        Path directory;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            directory = fileSystem.getPath(resource.getNamespace() + "/" + resource.getPath());
        }
        else {
            directory = Paths.get(uri);
        }

        // Always load internal textures first
        Texture missing = Texture.MISSING;
        missing.updateTexture(new Resource("engine", "textures/internal/missing.jpg"));
        this.textures.put(new Resource("engine", "internal/missing"), missing);

        Texture uv_test = Texture.UV_TEST;
        uv_test.updateTexture(new Resource("engine", "textures/internal/uv_test.jpg"));
        this.textures.put(new Resource("engine", "internal/uv_test"), uv_test);

        // Load all textures
        try (Stream<Path> walk = Files.walk(directory, 3).filter(Files::isRegularFile)) {
            for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                // The texture file source
                Path path = it.next();
                Path localPath = path.subpath(directory.getNameCount(), path.getNameCount());
                String fileName = localPath.subpath(0, localPath.getNameCount() - 1).toString().replace("\\", "/")
                        + "/" + localPath.getFileName().toString();
                Resource textureSource = resource.relative(fileName);

                // The resource that will be used later to fetch textures from the map (namespace:texture_file_name)
                Resource texture = new Resource(
                        resource.getNamespace(),
                        fileName.replaceFirst("[.][^.]+$", ""));

                if (this.textures.containsKey(texture))
                    continue;

                this.logger.log(LogLevel.INFO, "Loading texture %s from %s", texture, textureSource);
                this.textures.put(texture, loadTexture(textureSource));
            }
        }
    }

    public void reload() throws EngineException, IOException {
        Engine.getInstance().getLogger().log(LogLevel.INFO, "Reloading textures for all namespaces");

        // Cleanup all textures
        for (Texture texture: this.textures.values()) {
            if (texture.getTextureId() == Texture.MISSING.getTextureId())
                continue;
            glDeleteTextures(texture.getTextureId());
        }
        this.textures.clear();

        // Initialize textures again
        for (Resource namespace: this.allNamespaces)
            this.loadNamespace(namespace);
    }
}
