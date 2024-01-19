package dev.artingl.Engine.resources;

import dev.artingl.Game.GameDirector;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Resource {

    private final String namespace;
    private final String path;

    public Resource(String fullPath) {
        this(fullPath.split(":")[0], fullPath.split(":")[1]);
    }

    public Resource(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    public String readAsString() throws IOException {
        return new String(readAsBytes(), StandardCharsets.UTF_8);
    }

    public byte[] readAsBytes() throws IOException {
        try (DataInputStream stream = load()) {
            return stream.readAllBytes();
        }
    }

    public DataInputStream load() throws IOException {
        InputStream stream = GameDirector.class.getResourceAsStream("/" + namespace + "/" + path);
        if (stream == null)
            throw new IOException("Unable to load file " + this);

        return new DataInputStream(stream);
    }

    @Nullable
    public URI getURI() {
        try {
            URL url = GameDirector.class.getResource("/" + namespace + "/" + path);
            if (url != null)
                return url.toURI();
        } catch (URISyntaxException ignored) { }

        return null;
    }

    /**
     * Make new resource relative to this one
     *
     * @param path Path that would be added to this resource
     * */
    public Resource relative(String path) {
        return new Resource(namespace, this.path + "/" + path);
    }

    /**
     * Check if the resource is valid
     * */
    public boolean exists() {
        return getURI() != null;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Resource))
            return false;
        return obj.toString().equals(toString());
    }
}
