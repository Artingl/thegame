#version 330 core

layout (location = 0) out vec4 fragColor;

uniform sampler2D fbTex;
uniform sampler2D ppTex;
uniform sampler2D depthTex;
uniform vec3 screenResolution;
uniform float ssaoKernel;

in vec2 uv;

void main() {
    // Apple box blur on the occlusion
    vec3 clr = vec3(0, 0, 0);
    vec2 pixSize = vec2(1.0 / screenResolution.x, 1.0 / screenResolution.y);
    float cnt = 0.0;
    for (float i = -ssaoKernel; i <= ssaoKernel; ++i) {
        for (float j = -ssaoKernel; j <= ssaoKernel; ++j) {
            clr += texture(ppTex, uv + vec2(i, j) * pixSize).rgb;
            cnt += 1.0f;
        }
    }
    clr /= cnt;
    fragColor = texture(fbTex, uv) * vec4(clr, 1);
}