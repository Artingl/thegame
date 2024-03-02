#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D fbTex;
uniform sampler2D ppTex;

in vec2 uv;

void main() {
    fragColor = texture(ppTex, uv);
}