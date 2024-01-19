package dev.artingl.Engine.renderer.shader;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.resources.Resource;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final Resource resource;
    private final ShaderType type;

    private int shaderId;

    public Shader(ShaderType type, Resource resource) {
        this.resource = resource;
        this.type = type;
        this.shaderId = -1;
    }

    public Resource getResource() {
        return resource;
    }

    public ShaderType getType() {
        return type;
    }

    /**
     * Delete the shader
     */
    public void cleanup() {
        if (this.shaderId == -1) {
            // Shader not initialized
            return;
        }

        glDeleteShader(this.shaderId);
        this.shaderId = -1;
    }

    /**
     * Compile the shader, so it can be used in the shader program
     *
     * @return The OpenGL shader ID or -1 on error
     */
    public int compile() {
        if (this.shaderId != -1) {
            // Shader already compiled
            return this.shaderId;
        }

        Logger logger = Engine.getInstance().getLogger();
        this.shaderId = glCreateShader(this.type.id);

        try {
            // Try to read the shader's code and compile it
            glShaderSource(this.shaderId, resource.readAsString());
            glCompileShader(this.shaderId);

            if (glGetShaderi(this.shaderId, GL_COMPILE_STATUS) == 0) {
                /* We're unable to compile the shader.
                 * Get the error message and throw an exception, so it'll cleanup everything.
                 */
                int ln = glGetShaderi(this.shaderId, GL_INFO_LOG_LENGTH);
                String errMsg = glGetShaderInfoLog(this.shaderId, ln);
                throw new EngineException(errMsg);
            }

            /* At this point we successfully compiled the shader.
             * Just go on and return the ID at the bottom of the function.
             */
        } catch (IOException | EngineException e) {
            // Print the exception and cleanup everything
            cleanup();
            logger.exception(e, "Unable to initialize the shader \"%s\"", resource);
        }

        return this.shaderId;
    }

}
