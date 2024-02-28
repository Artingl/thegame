#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D framebufferTexture;
uniform float opacity;
uniform vec4 color;

in vec3 worldPosition;
in vec3 normal;
in vec2 uv;

void main() {
    fragColor = texture(texture0, uv) * color;
//    fragColor = vec4(normal, 1);
    fragColor.a *= opacity;
}
