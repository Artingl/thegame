#version 330 core
#define colorRange 24.0

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D framebufferTexture;
uniform sampler2D postprocessingFramebufferTexture;
uniform vec3 screenResolution;

in vec3 worldPosition;
in vec2 uv;

void main() {
    fragColor = vec4(1, 0, 1, 1);//texture(texture0, uv);
}