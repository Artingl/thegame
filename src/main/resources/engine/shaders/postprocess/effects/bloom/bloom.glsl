#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D fbTex;
uniform sampler2D ppTex;
uniform vec3 screenResolution;

in vec2 uv;

#define THRESHOLD 1

void main() {
    // ppTex should be our blurred buffer
    vec4 clr = max(texture(ppTex, uv) - THRESHOLD, 0);
    clr.a = 1;

    fragColor = texture(fbTex, uv) + clr;
}