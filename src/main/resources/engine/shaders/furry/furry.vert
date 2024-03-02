#version 330 core

#define PI 3.14159265359

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv;

uniform vec3 m_pos;
uniform vec3 m_rot;
uniform mat4 m_proj;
uniform mat4 m_model;
uniform mat4 m_view;
uniform float m_time;
uniform float m_layersStep;
uniform float m_totalLayers;
uniform int m_objType;

out vec3 normal;
out vec2 uv;
out float instanceId;

void main() {
    instanceId = gl_InstanceID / m_totalLayers;
    vec3 fragPosition = in_position;

    if (m_objType == 0)
        fragPosition.y += m_layersStep * instanceId;
    else
        fragPosition += in_normal * m_layersStep * instanceId;

    float angle = m_rot.y * PI / 180;
//    mat3 rotationMat = mat3(
//    cos(angle), 0, sin(angle),
//    0, 1, 0,
//    -sin(angle), 0, cos(angle)
//    );
    mat3 rotationMat = mat3(
        cos(angle), fragPosition.y, sin(angle),
        fragPosition.x, 1, fragPosition.z,
        -sin(angle), fragPosition.y, cos(angle)
    );
//    gl_Position = m_proj * m_view * (m_model * vec4(fragPosition, 1.0)) * rotationMat;
    gl_Position = m_proj * m_view * m_model * vec4(fragPosition, 1.0);

    normal = in_normal;
    uv = in_uv;
}