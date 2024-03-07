package dev.artingl.Engine.renderer;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Color;
import org.joml.Vector4f;

import static com.jme3.renderer.opengl.GL.GL_DEPTH_COMPONENT;
import static com.jme3.renderer.opengl.GL.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_NONE;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.glDeleteTextures;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class Framebuffer {

    private int depthTexture, frameTexture, fbo, depthBuffer;
    private int currentWidth, currentHeight;
    private long sinceLastResize;

    public Framebuffer() {}

    public void init() {
        Display display = Engine.getInstance().getDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Initialize framebuffer and depth buffer
        this.fbo = glGenFramebuffers();
        this.frameTexture = glGenTextures();
        this.depthTexture = glGenTextures();
        this.depthBuffer = glGenBuffers();

        Engine.getInstance().getLogger().log(LogLevel.INFO, "Creating framebuffer: res=%dx%d, fbo=%d, fbTex=%d, depthTex=%d", width, height, fbo, frameTexture, depthTexture);

        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
        glBindTexture(GL_TEXTURE_2D, this.frameTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, 0);

        glBindTexture(GL_TEXTURE_2D, this.depthTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri (GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_RENDERBUFFER, this.depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.depthBuffer);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.frameTexture, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, this.depthTexture, 0);

        glDrawBuffers(new int[]{ GL_COLOR_ATTACHMENT0 });

        // Check buffer
        int status;
        if ((status = glCheckFramebufferStatus(GL_FRAMEBUFFER)) != GL_FRAMEBUFFER_COMPLETE) {
            this.cleanup();
            throw new EngineException("Bad framebuffer status: " + status);
        }

        this.currentWidth = width;
        this.currentHeight = height;
        this.sinceLastResize = System.currentTimeMillis();
    }

    public void cleanup() {
        glDeleteBuffers(this.depthBuffer);
        glDeleteTextures(this.frameTexture);
        glDeleteTextures(this.depthTexture);
        glDeleteFramebuffers(this.fbo);
    }

    public int getBufferId() {
        return fbo;
    }

    public int getDepthTexture() {
        return depthTexture;
    }

    public int getFrameTexture() {
        return frameTexture;
    }

    public boolean updateBuffer() {
        Display display = Engine.getInstance().getDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Reinitialize the buffer again if the screen has resized.
        // Also don't do it too often and check if at least 100 ms has passed
        if (this.currentWidth != width || this.currentHeight != height && this.sinceLastResize + 100 < System.currentTimeMillis()) {
            this.cleanup();
            this.init();
            this.currentWidth = width;
            this.currentHeight = height;
            this.sinceLastResize = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    public void clear(Renderer renderer, Color color) {
        Framebuffer currentBuffer = renderer.getCurrentFramebuffer();
        renderer.bindFramebuffer(this);
        Vector4f c = color.asVector4f();
        glClearColor(c.x, c.y, c.z, c.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderer.bindFramebuffer(currentBuffer);
    }
}
