package com.example.backgroundremoval.model;

public class HueComposer extends BlendComposer {

    public HueComposer(int width, int height, int[] src, int[] dst) {
        super(width, height, src, dst);
    }

    @Override
    public int[] compose() {
        int offset = 0;
        int[] result = new int[width * height];
        int[] colorResults = new int[4];
        float[] srcHSL = new float[3];
        float[] dstHSL = new float[3];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int src = srcPixels[offset];
                int dst = dstPixels[offset];

                if (src == 0) {
                    result[offset++] = dst;
                    continue;
                }

                int srcBlue = src & 0xFF;
                int srcGreen = (src >> 8) & 0xFF;
                int srcRed = (src >> 16) & 0xFF;
                int srcAlpha = (src >> 24) & 0xFF;

                int dstBlue = dst & 0xFF;
                int dstGreen = (dst >> 8) & 0xFF;
                int dstRed = (dst >> 16) & 0xFF;
                int dstAlpha = (dst >> 24) & 0xFF;

                ColorUtilities.RGBtoHSL(srcRed, srcGreen, srcBlue, srcHSL);
                ColorUtilities.RGBtoHSL(dstRed, dstGreen, dstBlue, dstHSL);

                ColorUtilities.HSLtoRGB(srcHSL[0], dstHSL[1], dstHSL[2], colorResults);
                colorResults[3] = Math.min(255, srcAlpha + dstAlpha - (srcAlpha * dstAlpha) / 255);

                int pixel = (colorResults[3] << 24) + ((colorResults[0] & 0xFF) << 16) + ((colorResults[1] & 0xFF) << 8) + (colorResults[2] & 0xFF);
                result[offset++] = pixel;
            }
        }
        return result;
    }
}
