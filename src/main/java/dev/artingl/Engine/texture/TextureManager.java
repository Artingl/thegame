package dev.artingl.Engine.texture;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.IEngineEvent;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;

public class TextureManager implements IEngineEvent {

    private final Logger logger;
    private final Map<Resource, Texture> textures;
    private final List<Resource> allNamespaces;

    public TextureManager(Logger logger) {
        this.logger = logger;
        this.textures = new ConcurrentHashMap<>();
        this.allNamespaces = new ArrayList<>();
    }

    public void init() {
        Engine.getInstance().subscribeEngineEvents(this);
    }

    public void cleanup() {
        Engine.getInstance().unsubscribeEngineEvents(this);
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

        // Load texture and find free area on the atlas
        BufferedImage texture = ImageIO.read(target.load());
        int textureId = glGenTextures();

        // Bind texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Make byte buffer for the atlas (RGBA)
        int width = texture.getWidth();
        int height = texture.getHeight();
        int capacity = width * height * 4;
        int[] pixels = new int[texture.getWidth() * texture.getHeight()];;
        ByteBuffer buffer = BufferUtils.createByteBuffer(capacity);

        texture.getRGB(0, 0, texture.getWidth(), texture.getHeight(), pixels, 0, texture.getWidth());
        for(int y = 0; y < texture.getHeight(); y++){
            for(int x = 0; x < texture.getWidth(); x++){
                int pixel = pixels[y * texture.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        // Send the buffer to opengl
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Set params and unbind
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        return new Texture(textureId);
    }

    /**
     * Returns total amount of loaded textures
     * */
    public int totalTextures() {
        return this.textures.size();
    }

    public Texture getTexture(Resource texture) {
        if (!textures.containsKey(texture))
            return Texture.EMPTY;

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

        // Load missing texture first
        this.textures.put(
                new Resource("engine", "internal/missing"),
                loadTexture(new Resource("engine", "textures/internal/missing.jpg")));

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

                this.logger.log(LogLevel.INFO, "Loading texture %s from %s", texture, textureSource);
                this.textures.put(texture, loadTexture(textureSource));
            }
        }
    }

    @Override
    public void onReload() throws EngineException, IOException {
        Engine.getInstance().getLogger().log(LogLevel.INFO, "Reloading textures for all namespaces");

        // Cleanup all textures
        for (Texture texture: this.textures.values())
            glDeleteTextures(texture.getTextureId());
        this.textures.clear();

        // Initialize textures again
        for (Resource namespace: this.allNamespaces)
            this.loadNamespace(namespace);
    }
}
