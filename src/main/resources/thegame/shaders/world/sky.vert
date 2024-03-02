#version 330 core

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv;

uniform mat4 m_proj;
uniform mat4 m_view;
uniform mat4 m_model;

out vec3 worldPosition;
out vec3 fragPosition;
out vec2 uv;

void main() {
    fragPosition = in_position;
    worldPosition = (m_model * vec4(in_position, 1.0)).xyz;
    gl_Position = m_proj * m_view * vec4(worldPosition, 1.0);
    uv = in_uv;
}