#version 330 core

#define SUN_COLOR vec3(0.98, 0.58, 0.003)
#define MOON_COLOR vec3(0.89, 0.89, 0.89)

#define PI 3.14159265358979323846

layout (location = 0) out vec4 fragColor;

uniform sampler2D tex0;
uniform float lightLevel;
uniform vec3 skyColor;
uniform float currentRadius;
uniform float m_time;

in vec3 fragPosition;
in vec3 worldPosition;
in vec2 uv;

float mod289(float x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x ){ return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 perm(vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
float rand(vec2 c) { return fract(sin(dot(c.xy ,vec2(12.9898,78.233))) * 43758.5453); }

// https://www.shadertoy.com/view/4tXyWN
float hash(uvec2 x) {
    uvec2 q = 1103515245U * ((x >> 1U) ^ (x.yx));
    uint  n = 1103515245U * ((q.x) ^ (q.y >> 3U));
    return float(n) * (1.0 / float(0xffffffffU));
}

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

float noise(vec2 p, float freq) {
    float unit = 12/freq;
    vec2 ij = floor(p/unit);
    vec2 xy = mod(p,unit)/unit;
    //xy = 3.*xy*xy-2.*xy*xy*xy;
    xy = .5*(1.-cos(PI*xy));
    float a = rand((ij+vec2(0.,0.)));
    float b = rand((ij+vec2(1.,0.)));
    float c = rand((ij+vec2(0.,1.)));
    float d = rand((ij+vec2(1.,1.)));
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

vec3 renderPlanet(float radius, vec3 position, vec3 color) {
    // Calculate color for the planet
    float planetDistance = distance(fragPosition, position);
    vec3 planetColor = vec3(0);
    if (planetDistance > radius - 1 && planetDistance < radius) {
        // stroke around the planet
        planetColor = color - 0.2f;
    }
    else if (planetDistance < radius) {
        planetColor = color - perlinNoise(fragPosition.xy + fragPosition.z + 100, 10) / 10;
    }

    return planetColor;
}

vec3 stars(vec2 iuv) {
    if (hash(uvec2(iuv)) < 0.003) {
        return vec3(10.0, 10.0, 10.0) * genericNoise(vec3(floor(iuv / 16) * 16, m_time));
    }
    return vec3(0.0, 0.0, 0.0);
}

void main() {
    vec3 starColor = stars(uv * currentRadius * 3);
    starColor *= 1 - lightLevel;

    // Calculate color for the sun and moon
    vec3 sunColor = renderPlanet(currentRadius * 0.1f, vec3(currentRadius, 0, 0), SUN_COLOR);
    vec3 moonColor = renderPlanet(currentRadius * 0.13f, vec3(-currentRadius, 0, 0), MOON_COLOR);
    if (sunColor.x != 0 || moonColor.x != 0) {
        starColor = vec3(0);
    }

    float light = min(1, lightLevel + 0.1f);
    fragColor = vec4((skyColor * light) + sunColor + moonColor + starColor, 1);
}
