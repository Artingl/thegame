#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D tex0;
uniform int isTex0Set;
uniform float opacity;
uniform vec4 color;

in vec3 worldPosition;
in vec3 normal;
in vec2 uv;

void main() {
    float v = min(1, 1 - (abs(normal.x + normal.z) / 16));
    fragColor = color * vec4(v, v, v, 1);
    if (isTex0Set == 1) {
        fragColor *= texture(tex0, uv);
    }
    fragColor.a *= opacity;
}
