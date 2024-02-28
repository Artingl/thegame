#version 330 core

#define PI 3.14159265358979323846

layout (location = 0) out vec4 fragColor;

uniform sampler2D texture0;
uniform float lightLevel;
uniform vec3 skyColor;
uniform vec3 cameraPosition;

in vec3 color;
in vec3 normal;
in vec3 worldPosition;

float mod289(float x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x){ return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 perm(vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
float rand(vec2 c) { return fract(sin(dot(c.xy, vec2(12.9898, 78.233))) * 43758.5453); }

float genericNoise(vec3 p) {
    vec3 a = floor(p);
    vec3 d = p - a;
    d = d * d * (3.0 - 2.0 * d);

    vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
    vec4 k1 = perm(b.xyxy);
    vec4 k2 = perm(k1.xyxy + b.zzww);

    vec4 c = k2 + a.zzzz;
    vec4 k3 = perm(c);
    vec4 k4 = perm(c + 1.0);

    vec4 o1 = fract(k3 * (1.0 / 41.0));
    vec4 o2 = fract(k4 * (1.0 / 41.0));

    vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
    vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

    return o4.y * d.y + o4.x * (1.0 - d.y);
}

float noise(vec2 p, float freq){
    float unit = 12/freq;
    vec2 ij = floor(p/unit);
    vec2 xy = mod(p, unit)/unit;
    //xy = 3.*xy*xy-2.*xy*xy*xy;
    xy = .5*(1.-cos(PI*xy));
    float a = rand((ij+vec2(0., 0.)));
    float b = rand((ij+vec2(1., 0.)));
    float c = rand((ij+vec2(0., 1.)));
    float d = rand((ij+vec2(1., 1.)));
    float x1 = mix(a, b, xy.x);
    float x2 = mix(c, d, xy.x);
    return mix(x1, x2, xy.y);
}

float perlinNoise(vec2 p, int res){
    float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    for (int i = 0; i<50; i++){
        n+=amp*noise(p, f);
        f*=2.;
        normK+=amp;
        amp*=persistance;
        if (iCount == res) break;
        iCount++;
    }
    float nf = n/normK;
    return nf*nf*nf*nf;
}

void main() {
    // Calculate the color with the shade based on the normal
    float colorInfluence = min(1, 1 - (abs(normal.x + normal.z) / 16));
    vec3 resultColor = color * colorInfluence;

    // Add some randomness to the mesh color
    resultColor *= 1 - (genericNoise(worldPosition)*0.05f);

    float light = min(1, lightLevel + 0.3f);
    fragColor = vec4(resultColor * light, 1);
}
