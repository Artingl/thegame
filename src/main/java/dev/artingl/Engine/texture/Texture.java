package dev.artingl.Engine.texture;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class Texture {

    public static final Texture EMPTY = new Texture(1);

    private final int textureId;
    private boolean isTiled;

    public Texture(int textureId) {
        this.textureId = textureId;
        this.isTiled = false;
    }

    @Override
    public String toString() {
        return "Texture{id=" + textureId + "}";
    }

    public void setTiling(boolean state) {
        if (state != this.isTiled) {
            glBindTexture(GL_TEXTURE_2D, textureId);

            if (state) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            }

            glBindTexture(GL_TEXTURE_2D, 0);
        }

        this.isTiled = state;
    }

    public int getTextureId() {
        return textureId;
    }

    public boolean isTiled() {
        return isTiled;
    }
}
