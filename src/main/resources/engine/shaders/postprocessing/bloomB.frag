#version 330 core
#define colorRange 32.0

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D framebufferTexture;
uniform sampler2D postprocessingFramebufferTexture;
uniform vec3 screenResolution;

in vec3 worldPosition;
in vec2 uv;

vec4 jodieReinhardTonemap(vec4 c) {
    float l = dot(c, vec4(0.2126, 0.7152, 0.0722, 1));
    vec4 tc = c / (c + 1.0);

    return mix(c / (l + 1.0), tc, tc);
}

vec4 bloomTile(float lod, vec2 offset, vec2 uv) {
    return texture(postprocessingFramebufferTexture, uv * exp2(-lod) + offset);
}

vec4 getBloom(vec2 uv) {
    vec4 blur = vec4(0.0);

    blur = pow(bloomTile(2., vec2(0.0,0.0), uv),vec4(2.2))       	   	+ blur;
    blur = pow(bloomTile(3., vec2(0.3,0.0), uv),vec4(2.2)) * 1.3        + blur;
    blur = pow(bloomTile(4., vec2(0.0,0.3), uv),vec4(2.2)) * 1.6        + blur;
    blur = pow(bloomTile(5., vec2(0.1,0.3), uv),vec4(2.2)) * 1.9 	   	+ blur;
    blur = pow(bloomTile(6., vec2(0.2,0.3), uv),vec4(2.2)) * 2.2 	   	+ blur;

    return blur * colorRange;
}
void main() {
    vec4 color = pow(texture(framebufferTexture, uv) * colorRange, vec4(2.2));
    color = pow(color, vec4(2.2));
    color += pow(getBloom(uv), vec4(2.2));
    color = pow(color, vec4(1.0 / 2.2));

    fragColor = jodieReinhardTonemap(color);
}