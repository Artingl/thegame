package dev.artingl.Engine.resources.texture;

import dev.artingl.Engine.resources.Resource;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {

    public static final Texture MISSING = new Texture(null);

    private BufferedImage texture;
    private int textureId;
    private boolean isTiled;
    private boolean updateParams;
    private boolean updateTexture;
    private long lastUpdate;

    public Texture(BufferedImage texture) {
        this.texture = texture;
        this.textureId = -1;
        this.isTiled = false;
        this.updateParams = false;
        this.updateTexture = true;
    }

    /**
     * Update texture
     * */
    public void updateTexture(BufferedImage texture) {
        if (texture == null)
            return;
        this.texture = texture;
        this.updateTexture = true;
    }

    /**
     * Update texture
     * */
    public void updateTexture(Resource resource) throws IOException {
        if (resource == null || !resource.exists())
            return;
        this.texture = ImageIO.read(resource.load());
        this.updateTexture = true;
    }

    @Override
    public String toString() {
        return "Texture{id=" + textureId + "}";
    }

    public void setTiling(boolean state) {
        if (state != this.isTiled)
            this.updateParams = true;
        this.isTiled = state;
    }

    public int getTextureId() {
        this.update();
        return textureId;
    }

    public boolean isTiled() {
        return isTiled;
    }

    private void update() {
        // Update the texture if something has changed.
        // Do the update only once every 100 ms, so we don't waste too many resources
        long now = System.currentTimeMillis();
        if (this.lastUpdate + 50 < now) {
            this.lastUpdate = now;

            if (this.updateTexture) {
                this.updateTexture = false;
                if (this.textureId == -1)
                    this.textureId = glGenTextures();

                // Bind texture
                glBindTexture(GL_TEXTURE_2D, textureId);

                int textureType = texture.getType();
                int bpp = textureType == BufferedImage.TYPE_INT_RGB ? 3 : 4;
                int textureFormat = bpp == 3 ? GL_RGB : GL_RGBA;

                // Make byte buffer for the texture (RGBA)
                int width = texture.getWidth();
                int height = texture.getHeight();
                int capacity = width * height * bpp;
                int[] pixels = new int[texture.getWidth() * texture.getHeight()];
                ByteBuffer buffer = BufferUtils.createByteBuffer(capacity);

                this.texture.getRGB(0, 0, texture.getWidth(), texture.getHeight(), pixels, 0, texture.getWidth());
                for (int y = 0; y < texture.getHeight(); y++) {
                    for (int x = 0; x < texture.getWidth(); x++) {
                        int pixel = pixels[y * texture.getWidth() + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF));
                        buffer.put((byte) ((pixel >> 8) & 0xFF));
                        buffer.put((byte) (pixel & 0xFF));
                        if (bpp > 3)
                            buffer.put((byte) ((pixel >> 24) & 0xFF));
                    }
                }
                buffer.flip();

                // Send the buffer to opengl
                glTexImage2D(GL_TEXTURE_2D, 0, textureFormat, texture.getWidth(), texture.getHeight(), 0, textureFormat, GL_UNSIGNED_BYTE, buffer);

                // Set params and unbind
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glBindTexture(GL_TEXTURE_2D, 0);
            }

            if (this.updateParams) {
                this.updateParams = false;
                glBindTexture(GL_TEXTURE_2D, textureId);

                if (isTiled) {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                } else {
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
                }

                glBindTexture(GL_TEXTURE_2D, 0);
            }
        }
    }
}
