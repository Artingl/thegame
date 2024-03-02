package dev.artingl.Engine.renderer;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;

import static com.jme3.renderer.opengl.GL.GL_DEPTH_COMPONENT;
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
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.glDeleteTextures;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

public class Framebuffer {

    private int depthTexture, frameTexture, framebuffer, depthBuffer;
    private int currentWidth, currentHeight;
    private long sinceLastResize;

    public Framebuffer() {}

    public void init() {
        Display display = Engine.getInstance().getDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Initialize framebuffer and depth buffer
        this.framebuffer = glGenFramebuffers();
        this.frameTexture = glGenTextures();
        this.depthTexture = glGenTextures();
        this.depthBuffer = glGenBuffers();

        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);

        glBindBuffer(GL_RENDERBUFFER, this.depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.depthBuffer);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.frameTexture, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, this.depthTexture, 0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});

        // Check framebuffer
        int status;
        if ((status = glCheckFramebufferStatus(GL_FRAMEBUFFER)) != GL_FRAMEBUFFER_COMPLETE)
            throw new EngineException("Bad framebuffer status: " + status);

        this.currentWidth = width;
        this.currentHeight = height;
        this.sinceLastResize = System.currentTimeMillis();
    }

    public void cleanup() {
        glDeleteBuffers(this.depthBuffer);
        glDeleteTextures(this.frameTexture);
        glDeleteTextures(this.depthTexture);
        glDeleteFramebuffers(this.framebuffer);
    }

    public int getFramebuffer() {
        return framebuffer;
    }

    public int getDepthBuffer() {
        return depthBuffer;
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

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0, 0, 0, 1);
    }
}
