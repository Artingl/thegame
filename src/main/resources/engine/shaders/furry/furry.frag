#version 330 core

#define PI 3.14159265358979323846

layout (location = 0) out vec4 fragColor;

uniform float opacity;
uniform vec3 furColor;
uniform vec4 color;
uniform int density;

in vec3 normal;
in vec2 uv;
in float instanceId;

const uint k = 1103515245U;
vec3 hash(uvec3 x) {
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;
    return vec3(x)*(1.0/float(0xffffffffU));
}

#define BLADE_RADIUS 0.4f

void main() {
    uvec2 v = uvec2(uv * density);
    vec2 bladeUV = mod(uv, 1f / density) / (1f / density);
    float bladeWidth = distance(bladeUV.x, 0.5f);
    float bladeHeight = distance(bladeUV.y, 0.5f);
    float hashResult = hash(uvec3(v, 0)).x;
    if (hashResult < instanceId || bladeHeight > 0.1f || bladeWidth > BLADE_RADIUS)
        discard;

    fragColor = color;
    fragColor *= vec4(furColor * instanceId, 1);
//    fragColor.a *= opacity;
}
