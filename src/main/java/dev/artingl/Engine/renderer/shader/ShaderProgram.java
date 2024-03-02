package dev.artingl.Engine.renderer.shader;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.Framebuffer;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram implements TickListener {


    private final Shader[] shadersList;
    private final List<TextureUniform> textures;
    private final List<UniformProp> uniforms;
    private int programId;
    private int mainTexture;
    private float time;

    public ShaderProgram(Shader ...shaders) {
        this.textures = new ArrayList<>();
        this.uniforms = new ArrayList<>();
        this.shadersList = shaders;
        this.programId = -1;
    }

    /**
     * Bake the shader program to use it later in the pipeline.
     * */
    public void bake() throws EngineException {
        if (this.programId != -1) {
            // Already baked
            return;
        }

        Engine.getInstance().getTimer().subscribe(this);

        this.programId = glCreateProgram();

        // Compile all shaders
        for (Shader shader: shadersList) {
            int id = shader.compile();

            if (id == -1) {
                /* We do not need to cleanup the program, because after making
                 * this exception the pipeline manager will call pipelineCleanup.
                 */
                throw new EngineException("Unable to initialize shader program!");
            }

            // We got valid shader, attach it to the program
            glAttachShader(programId, id);
        }

        // Link the program
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            // We're unable to link the program. Get the error message and throw an exception
            int ln = glGetProgrami(programId, GL_INFO_LOG_LENGTH);
            String errMsg = glGetProgramInfoLog(programId, ln);

            // Again, the same thing as for the EngineException above. We don't need to call cleanup here
            throw new EngineException(errMsg);
        }

        // Cleanup all shaders because we successfully linked the shader program
        for (Shader shader: shadersList) {
            shader.cleanup();
        }

        Logger logger = Engine.getInstance().getLogger();
        logger.log(LogLevel.INFO, "Baked shader program: " + this);
    }

    @Override
    public String toString() {
        StringBuilder resources = new StringBuilder();

        // Create the resources string based on all shaders of this program
        for (Shader shader: shadersList) {
            resources.append(shader.getResource()).append(", ");
        }

        return "ShaderProgram{" + resources.substring(0, resources.length() - 2) + "}";
    }

    /**
     * Cleanup everything related to this program
     * */
    public void cleanup() {
        Engine.getInstance().getTimer().unsubscribe(this);
        this.textures.clear();

        // Cleanup all shaders just in case
        for (Shader shader: shadersList) {
            shader.cleanup();
        }

        if (this.programId == -1) {
            // Not initialized
            return;
        }

        glDeleteProgram(this.programId);
        this.programId = -1;
    }

    /**
     * Tells is the program has been already baked or not
     * */
    public boolean isBaked() {
        return this.programId != -1;
    }

    public int getUniformLoc(String name)
    {
        return glGetUniformLocation(this.programId, name);
    }

    public void setUniformMatrix4f(String uniform, Matrix4f mat)  {
        this.uniforms.add(new UniformProp(uniform, mat));
    }

    public void setUniformObject(String uniform, Object o) {
        this.uniforms.add(new UniformProp(uniform, o));
    }

    public void setUniformFloat(String uniform, float f) {
        this.uniforms.add(new UniformProp(uniform, f));
    }

    public void setUniformVector3f(String uniform, Vector3f vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void setUniformVector4f(String uniform, Vector4f vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void setUniformVector4i(String uniform, Vector4i vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void setUniformVector2f(String uniform, Vector2f vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void setUniformInt(String uniform, int f) {
        this.uniforms.add(new UniformProp(uniform, f));
    }

    public void setUniformVector3i(String uniform, Vector3i vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void setUniformVector2i(String uniform, Vector2i vec) {
        this.uniforms.add(new UniformProp(uniform, vec));
    }

    public void updateViewport(Viewport viewport) {
        this.uniforms.add(new UniformProp("m_proj", viewport.getProjection()));
        this.uniforms.add(new UniformProp("m_view", viewport.getView()));
        this.uniforms.add(new UniformProp("m_pos", viewport.getPosition()));
        this.uniforms.add(new UniformProp("m_rot", viewport.getRotation()));
    }

    public void updateModelMatrix(Matrix4f model) {
        this.uniforms.add(new UniformProp("m_model", model));
    }

    public void activateTexture(String uniform, int offset, int id) {
        glUniform1i(getUniformLoc(uniform), offset);
        glActiveTexture(GL_TEXTURE0 + offset);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void setTextureUniform(String uniform, int id) {
        this.textures.add(new TextureUniform(uniform, id));
    }

    public void setMainTexture(Texture texture) {
        if (texture == null) {
            this.mainTexture = 0;
        }
        else this.mainTexture = texture.getTextureId();
    }

    public int getProgramId() {
        return programId;
    }

    public void use() {
        Engine engine = Engine.getInstance();
        Renderer renderer = engine.getRenderer();
        Framebuffer framebuffer = renderer.getMainFramebuffer();
        renderer.useShader(this);

        glUniform3f(getUniformLoc("screenResolution"), engine.getDisplay().getWidth(), engine.getDisplay().getHeight(), engine.getDisplay().getAspectRatio());
        glUniform1f(getUniformLoc("m_time"), time);

        // Setup and bind textures for the shader
        this.activateTexture("tex0", 0, mainTexture);
        this.uniforms.add(new UniformProp("isTex0Set", mainTexture > 0));
        this.mainTexture = Texture.MISSING.getTextureId();

        // Set all uniforms
        for (UniformProp uniform: uniforms) {
            int loc = getUniformLoc(uniform.name);
            if (uniform.value instanceof Matrix4f mat) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer fb = mat.get(stack.mallocFloat(16));
                    glUniformMatrix4fv(loc, false, fb);
                }
            }
            else if (uniform.value instanceof Float v) glUniform1f(loc, v);
            else if (uniform.value instanceof Integer v) glUniform1i(loc, v);
            else if (uniform.value instanceof Boolean v) glUniform1i(loc, v ? 1 : 0);
            else if (uniform.value instanceof Vector2f v) glUniform2f(loc, v.x, v.y);
            else if (uniform.value instanceof Vector3f v) glUniform3f(loc, v.x, v.y, v.z);
            else if (uniform.value instanceof Vector2i v) glUniform2i(loc, v.x, v.y);
            else if (uniform.value instanceof Vector3i v) glUniform3i(loc, v.x, v.y, v.z);
            else if (uniform.value instanceof Vector4i v) glUniform4i(loc, v.x, v.y, v.z, v.w);
            else if (uniform.value instanceof Vector4f v) glUniform4f(loc, v.x, v.y, v.z, v.w);
        }
        this.uniforms.clear();

        int i = 1;
        for (TextureUniform tex: this.textures) {
            this.activateTexture(tex.uniform, i++, tex.textureId);
        }
        this.textures.clear();
        this.textures.add(new TextureUniform("fbTex", framebuffer.getFrameTexture()));
        this.textures.add(new TextureUniform("depthTex", framebuffer.getDepthTexture()));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShaderProgram shd))
            return false;
        return shd.programId == programId;
    }

    @Override
    public void tick(Timer timer) {
        this.time += 1f / timer.getTickPerSecond();
    }

    private record TextureUniform(String uniform, int textureId) {}

    private record UniformProp(String name, Object value) {}

}
