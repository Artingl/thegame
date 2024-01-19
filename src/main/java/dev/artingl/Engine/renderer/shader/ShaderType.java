package dev.artingl.Engine.renderer.shader;

import static org.lwjgl.opengl.GL20.*;

public enum ShaderType {

    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER),

    ;

    public final int id;

    ShaderType(int id) {
        this.id = id;
    }

}
