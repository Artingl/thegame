#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D fbTex;
uniform sampler2D ppTex;
uniform sampler2D depthTex;
uniform vec3 screenResolution;
uniform float farPlane;
uniform float nearPlane;
uniform float m_time;

uniform float ssaoKernel;
uniform float ssaoMaxDistance;
uniform float ssaoBias;

in vec2 uv;

float linearizeDepth(float depth) {
    return (2.0 * nearPlane * farPlane) / (farPlane + nearPlane - (depth * 2.0 - 1.0) * (farPlane - nearPlane));
}

float getDepthValue(vec2 pos) {
    return linearizeDepth(texture(depthTex, pos).r) / farPlane;
}

void main() {
    vec2 pixSize = 1.0 / vec2(textureSize(depthTex, 0)) * 6;
    float occlusionFactor = 1;
    float radius = ssaoKernel * 2;
    float pixDepth = getDepthValue(uv);

    // Calculate occlusion factor
    if (pixDepth < ssaoMaxDistance / farPlane) {
        for (float i = -ssaoKernel; i <= ssaoKernel; i++) {
            for (float j = -ssaoKernel; j <= ssaoKernel; j++) {
                vec2 offset = vec2(i, j);
                float contribDepth = getDepthValue(uv + offset * pixSize);
                float rangeCheck = smoothstep(0.0, 1.0, radius / abs(contribDepth - pixDepth));

                if (distance(offset, vec2(0)) < radius) {
                    if (contribDepth <= pixDepth + ssaoBias) {
                        occlusionFactor += 1 * rangeCheck;
                    }
                }
            }
        }

        occlusionFactor = 1 - (occlusionFactor / (ssaoKernel * 2 * ssaoKernel * 16));
        occlusionFactor = clamp(occlusionFactor, 0, 1);
    }

    fragColor = vec4(occlusionFactor, occlusionFactor, occlusionFactor, 1);
}