package com.example.backgroundremoval.model;

public abstract class BlendComposer {

    protected int height;
    protected int width;
    protected int[] srcPixels;
    protected int[] dstPixels;

    public BlendComposer(int width, int height, int[] src, int[] dst) {
        this.width = width;
        this.height = height;
        this.srcPixels = src;
        this.dstPixels = dst;
    }

    /**
     * Blend 2 pixels.
     *
     * @return blended pixels
     */
    public abstract int[] compose();
}
