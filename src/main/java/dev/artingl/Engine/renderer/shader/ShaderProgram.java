package dev.artingl.Engine.renderer.shader;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.pipeline.IPipeline;
import dev.artingl.Engine.renderer.pipeline.PipelineInstance;
import dev.artingl.Engine.resources.texture.Texture;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram implements IPipeline {


    private final Shader[] shadersList;
    private final List<TextureUniform> textures;
    private int programId;
    private int mainTexture;

    public ShaderProgram(Shader ...shaders) {
        this.textures = new ArrayList<>();
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

    public void setUniformMatrix4f(String uniform, Matrix4f mat)
    {
        if (mat == null)
            return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = mat.get(stack.mallocFloat(16));
            glUniformMatrix4fv(getUniformLoc(uniform), false, fb);
        }
    }

    public void setUniformFloat(String uniform, float f)
    {
        glUniform1f(getUniformLoc(uniform), f);
    }

    public void setUniformVector3f(String uniform, Vector3f vec) {
        glUniform3f(getUniformLoc(uniform), vec.x, vec.y, vec.z);
    }

    public void setUniformVector2f(String uniform, Vector2f vec) {
        glUniform2f(getUniformLoc(uniform), vec.x, vec.y);
    }

    public void setUniformInt(String uniform, int f)
    {
        glUniform1i(getUniformLoc(uniform), f);
    }

    public void setUniformVector3i(String uniform, Vector3i vec) {
        glUniform3i(getUniformLoc(uniform), vec.x, vec.y, vec.z);
    }

    public void setUniformVector2i(String uniform, Vector2i vec) {
        glUniform2i(getUniformLoc(uniform), vec.x, vec.y);
    }

    public void updatePVMatrix(Matrix4f proj, Matrix4f view) {
        setUniformMatrix4f("m_proj", proj);
        setUniformMatrix4f("m_view", view);
    }

    public void updateModelMatrix(Matrix4f model) {
        setUniformMatrix4f("m_model", model);
    }

    public void activateTexture(String uniform, int offset, int id) {
        glUniform1i(getUniformLoc(uniform), offset);
        glActiveTexture(GL_TEXTURE0 + offset);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void addTextureUniform(String uniform, int id) {
        this.textures.add(new TextureUniform(uniform, id));
    }

    public void setMainTexture(Texture texture) {
        this.mainTexture = texture.getTextureId();
    }

    public int getProgramId() {
        return programId;
    }

    public void use() {
        Engine engine = Engine.getInstance();
        Renderer renderer = engine.getRenderer();
        renderer.useShader(this);

        glUniform3f(getUniformLoc("screenResolution"), engine.getDisplay().getWidth(), engine.getDisplay().getHeight(), engine.getDisplay().getAspectRatio());

        // Setup and bind textures for the shader
//        System.out.println(mainTexture);
        this.activateTexture("texture0", 0, mainTexture);
        this.activateTexture("framebufferTexture", 1, renderer.getRenderTextureId());
        this.mainTexture = Texture.MISSING.getTextureId();

        int i = 2;
        for (TextureUniform tex: this.textures) {
            this.activateTexture(tex.uniform, i++, tex.textureId);
        }
    }

    @Override
    public void pipelineInit(PipelineInstance instance) throws EngineException {
        bake();
    }

    @Override
    public void pipelineCleanup(PipelineInstance instance) {
        cleanup();
    }

    @Override
    public void pipelineRender(RenderContext renderContext, PipelineInstance instance) {
        this.use();
    }

    @Override
    public int pipelineFlags() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShaderProgram shd))
            return false;
        return shd.programId == programId;
    }

    private record TextureUniform(String uniform, int textureId) {}

}
