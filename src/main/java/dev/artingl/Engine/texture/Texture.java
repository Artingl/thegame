package dev.artingl.Engine.texture;

public record Texture(int textureId) {

    public static final Texture EMPTY = new Texture(1);

    @Override
    public String toString() {
        return "Texture{id=" + textureId + "}";
    }
}
