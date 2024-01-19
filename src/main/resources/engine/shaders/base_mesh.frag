#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D framebufferTexture;
uniform float meshFade;

in vec3 worldPosition;
in vec4 color;
in vec2 uv;

void main() {
    fragColor = texture(texture0, uv) * color;
    fragColor.a *= meshFade;
}
