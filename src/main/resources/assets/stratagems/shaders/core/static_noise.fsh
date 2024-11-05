#version 150

#moj_import <stratagems:static_noise.glsl>

uniform vec4 ColorModulator;
uniform float GameTime;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    // Generate a black to white pixel
    vec3 noiseColor = vec3(noise(texCoord0, GameTime));

    // Output to screen
    fragColor = vec4(noiseColor, 1.0) * ColorModulator;
}