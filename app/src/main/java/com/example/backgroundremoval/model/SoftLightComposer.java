package com.example.backgroundremoval.model;

class SoftLightComposer extends BlendComposer {
    /**
     * Create a SoftLightComposer.
     */
    public SoftLightComposer(int width, int height, int[] src, int[] dst) {
        super(width, height, src, dst);
    }

    @Override
    public int[] compose() {
        int offset = 0;
        int[] result = new int[width * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int src = srcPixels[offset];
                int dst = dstPixels[offset];

                if (dst == 0) {
                    result[offset++] = src;
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

                int mRed = srcRed * dstRed / 255;
                int mGreen = srcGreen * dstGreen / 255;
                int mBlue = srcBlue * dstBlue / 255;
                int red = mRed + srcRed * (255 - ((255 - srcRed) * (255 - dstRed) / 255) - mRed) / 255;
                int green = mGreen + srcGreen * (255 - ((255 - srcGreen) * (255 - dstGreen) / 255) - mGreen) / 255;
                int blue = mBlue + srcBlue * (255 - ((255 - srcBlue) * (255 - dstBlue) / 255) - mBlue) / 255;
                int alpha = Math.min(255, srcAlpha + dstAlpha - (srcAlpha * dstAlpha) / 255);

                int pixel = (alpha << 24) + ((red & 0xFF) << 16) + ((green & 0xFF) << 8) + (blue & 0xFF);
                result[offset++] = pixel;
            }
        }

        return result;
    }
}
