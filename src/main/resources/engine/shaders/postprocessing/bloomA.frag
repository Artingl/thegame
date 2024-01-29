#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform sampler2D framebufferTexture;
uniform vec3 screenResolution;

in vec3 worldPosition;
in vec2 uv;

vec4 makeBloom(float lod, vec2 offset, vec2 bCoord) {
    vec2 pixelSize = 1.0 / vec2(screenResolution.x, screenResolution.y);
    float lodFactor = exp2(lod);

    offset += pixelSize;

    vec4 bloom = vec4(0.0);
    vec2 scale = lodFactor * pixelSize;

    vec2 coord = (bCoord.xy-offset)*lodFactor;
    float totalWeight = 0.0;

    if (any(greaterThanEqual(abs(coord - 0.5), scale + 0.5))) {
        return vec4(0.0);
    }

    for (int i = -5; i < 5; i++) {
        for (int j = -5; j < 5; j++) {
            float wg = pow(1.0-length(vec2(i,j)) * 0.125, 6.0);

            bloom = pow(texture(framebufferTexture,vec2(i,j) * scale + lodFactor * pixelSize + coord, lod), vec4(2.2))*wg + bloom;
            totalWeight += wg;
        }
    }

    bloom /= totalWeight;
    return bloom;
}

void main() {
    vec2 uv = gl_FragCoord.xy / screenResolution.xy;

    vec4 blur = makeBloom(2.,vec2(0.0,0.0), uv);
    blur += makeBloom(3.,vec2(0.3,0.0), uv);
    blur += makeBloom(4.,vec2(0.0,0.3), uv);
    blur += makeBloom(5.,vec2(0.1,0.3), uv);
    blur += makeBloom(6.,vec2(0.2,0.3), uv);

    fragColor = pow(blur, vec4(1.0 / 2.2));
}