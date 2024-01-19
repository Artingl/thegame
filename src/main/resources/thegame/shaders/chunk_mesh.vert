#version 330 core

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec3 in_color;

uniform mat4 m_proj;
uniform mat4 m_view;
uniform mat4 m_model;

out vec3 color;
out vec3 normal;
out vec3 worldPosition;

void main() {
    color = in_color;
    normal = in_normal;
    worldPosition = (m_model * vec4(in_position, 1.0)).xyz;
    gl_Position = m_proj * m_view * vec4(worldPosition, 1.0);
}