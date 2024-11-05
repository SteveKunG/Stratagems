#version 150

// Quick and dirty tv static noise
// Created by PelicanPolice in 2020-11-05
// https://www.shadertoy.com/view/3d3fR7
float noise(vec2 pos, float evolve) {
    // Loop the evolution (over a very long period of time).
    float e = fract(evolve * 1);

    // Coordinates
    float cx = pos.x * e;
    float cy = pos.y * e;

    // Generate a "random" black or white value
    return fract(23.0 * fract(2.0 / fract(fract(cx * 2.4 / cy * 23.0 + pow(abs(cy / 22.4), 3.3)) * fract(cx * evolve / pow(abs(cy), 0.05)))));
}