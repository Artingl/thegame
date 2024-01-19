#version 330 core

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec4 in_color;
layout (location = 2) in vec2 in_uv;

uniform mat4 m_proj;
uniform mat4 m_view;
uniform mat4 m_model;

out vec3 worldPosition;
out vec4 color;
out vec2 uv;

void main() {
    gl_Position = vec4(in_position, 1.0);

    color = in_color;
    uv = in_uv;
}