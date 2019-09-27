package com.example.backgroundremoval.lib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;

public class VisionSegmentResult extends VisionResult{

    private static final int MASK_RGB = 0x00FFFFFF;

    private static final int DEFAULT_ALPHA_VALUE = 180;
    private static final int ALPHA_SHIFT = 24;

    private int[][] classifications;
    private float[][] confidence;
    private MaskType[] maskTypes;
    private Size targetInferenceSize;
    private Size modelOutputSize;
    private int offsetX;
    private int offsetY;

    private float confidenceThreshold;
    private PreparedImage preparedImage;

    public VisionSegmentResult(VisionImage originalImage, PreparedImage preparedImage, VisionSegmentPredictorOptions options, MaskType[] maskTypes, Size targetInferenceSize, Size modelOutputSize, int offsetX, int offsetY, int[][] classifications, float[][] confidence) {
        super(originalImage);
        this.classifications = classifications;
        this.confidence = confidence;
        this.offsetX = offsetX;

        this.offsetY = offsetY;
        this.targetInferenceSize = targetInferenceSize;
        this.modelOutputSize = modelOutputSize;
        this.maskTypes = maskTypes;
        this.confidenceThreshold = options.getTargetConfidenceThreshold();
        this.preparedImage = preparedImage;
    }

    /**
     * Creates a bitmap of the model output with the classification drawn as an overlay.
     * <p>
     * This bitmap will have the same dimensions as the model output size.
     *
     * @return a bitmap with the model result.
     * @deprecated use
     */
    public Bitmap toBitmap() {
        Bitmap bitmap = preparedImage.getBitmapForModel();

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        // Print the mask overlay on the canvas (without the original image)
        drawAllMasks(canvas, DEFAULT_ALPHA_VALUE, modelOutputSize);
        return mutableBitmap;
    }


    /**
     * Get the confidence scores for each classification.
     * <p>
     * The dimensions on the output matrix match the model output size.
     *
     * @return a matrix[row][col] of float scores.
     */
    public float[][] getConfidenceScores() {
        return confidence;
    }

    /**
     * Get the raw mask classifications for each point on the model output.
     * <p>
     * The dimensions on the output matrix match the model output size.
     *
     * @return a matrix [row][col] of MaskTypes
     */
    public MaskType[][] getMaskClassifications() {
        MaskType[][] maskClassifications = new MaskType[modelOutputSize.getHeight()][modelOutputSize.getWidth()];
        for (int row = 0; row < modelOutputSize.getHeight(); row++) {
            for (int col = 0; col < modelOutputSize.getWidth(); col++) {
                int maskIndex = classifications[row][col];
                maskClassifications[row][col] = maskTypes[maskIndex];
            }
        }
        return maskClassifications;
    }

    /**
     * Create a bitmap of the overlay to apply to an image.
     * This will have the model output dimensions.
     * <p>
     * Default alpha value is 60.
     *
     * @return a bitmap of the overlay
     * @deprecated
     */
    @Deprecated
    public Bitmap createMaskOverlayBitmap() {
        return buildMultiClassMask();
    }

    /**
     * Create a bitmap of the overlay to apply to an image.
     * This will have the model output dimensions.
     *
     * @param maxAlpha - value between 0-255 for the overlay.
     * @return a bitmap of the overlay
     * @deprecated use
     */
    @Deprecated
    public Bitmap createMaskOverlayBitmap(int maxAlpha) {
        return buildMultiClassMask(maxAlpha, 1, confidenceThreshold);
    }

    /**
     * Create a mask of the overlay to apply to an image.
     * This will have the model output dimensions.
     * <p>
     * Default alpha value is 60.
     *
     * @return a bitmap of the overlay
     */
    public Bitmap buildMultiClassMask() {
        return buildMultiClassMask(DEFAULT_ALPHA_VALUE, 1, confidenceThreshold);
    }

    /**
     * Create a mask of the overlay to apply to an image.
     * This will have the model output dimensions.
     *
     * @param maxAlpha            - value between 0-255 for the overlay.
     * @param clippingScoresAbove - scores above this threshold will set the alpha value as 1
     * @param zeroingScoresBelow  - scores below this threshold will set the alpha value as 0
     * @return a bitmap of the overlay
     */
    public Bitmap buildMultiClassMask(int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        // Set the alpha colors for quick indexing
        int[] maskIndexToColor = new int[maskTypes.length];
        for (int i = 0; i < maskTypes.length; i++) {
            int color = maskTypes[i].getColorIdentifier();
            maskIndexToColor[i] = color;
        }

        int[] colors = getColorsFromMask(maskIndexToColor, maxAlpha, clippingScoresAbove, zeroingScoresBelow);
        return Bitmap.createBitmap(colors, modelOutputSize.getWidth(), modelOutputSize.getHeight(), Bitmap.Config.ARGB_8888);
    }

    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Cut out the masked layer from the original image and create a new bitmap.
     * If no pixels are associated with the mask type, return null.
     * <p>
     * Example:
     * <pre>{@code
     *  Bitmap personBitmap = result.buildSingleClassMask(MaskType.PERSON);
     *  if(person != null) {
     *      canvas.drawBitmap(personBitmap, ...);
     *  }
     * }</pre>
     *
     * @param maskType - the mask to create a bitmap of.
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     */
    public Bitmap buildSingleClassMask(MaskType maskType) {
        return buildSingleClassMask(maskType, DEFAULT_ALPHA_VALUE, 1, confidenceThreshold);
    }

    public Bitmap buildSingleClassMask(MaskType maskType, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        // Special case where the mask to find is none.
        int color = (maskType == MaskType.NONE) ? Color.BLACK : maskType.getColorIdentifier();
        return buildSingleClassMask(maskType, maxAlpha, clippingScoresAbove, zeroingScoresBelow, color);
    }


    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Cut out the masked layer from the original image and create a new bitmap.
     * If no pixels are associated with the mask type, return null.
     * <p>
     * Example:
     * <pre>{@code
     *  Bitmap personBitmap = result.buildSingleClassMask(MaskType.PERSON, .8f, .5f);
     *  if(person != null) {
     *      canvas.drawBitmap(personBitmap, ...);
     *  }
     * }</pre>
     *
     * @param maskType            - the mask type used in the alpha mask.
     * @param maxAlpha            - the maximum alpha value.
     * @param clippingScoresAbove - scores above this threshold will set the alpha value as the max alpha value
     * @param zeroingScoresBelow  - scores below this threshold will set the alpha value as 0
     * @param maskColor           - Set the mask color
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     */
    public Bitmap buildSingleClassMask(MaskType maskType, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow, int maskColor) {

        // Set the alpha colors for quick indexing
        int[] maskIndexToColor = new int[maskTypes.length];
        Log.d("FritzVisonSegmentResult ", "buildSingleClassMask: "+maskTypes.length);
        for (int i = 0; i < maskTypes.length; i++) {
            MaskType maskTypeToCompare = maskTypes[i];

            if (maskType == maskTypeToCompare) {
                maskIndexToColor[i] = maskColor;
            } else {
                maskIndexToColor[i] = 0;
            }
        }

        int[] colors = getColorsFromMask(maskIndexToColor, maxAlpha, clippingScoresAbove, zeroingScoresBelow);
        Bitmap bitmap = Bitmap.createBitmap(colors, modelOutputSize.getWidth(), modelOutputSize.getHeight(), Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    private int[] getColorsFromMask(int[] maskIndexToColor, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow) {
        int outputWidth = modelOutputSize.getWidth();
        int outputHeight = modelOutputSize.getHeight();
        int[] colors = new int[outputHeight * outputWidth];

        // Create an array of colors
        for (int row = 0; row < outputHeight; row++) {
            for (int col = 0; col < outputWidth; col++) {
                int maskTypeIndex = classifications[row][col];
                float pointConfidence = confidence[row][col];

                // If the color is transparent, set it and be done.
                if (maskIndexToColor[maskTypeIndex] == 0) {
                    colors[row * outputWidth + col] = 0;
                    continue;
                }

                if (pointConfidence > clippingScoresAbove) {
                    int color = (maxAlpha << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
                    colors[row * outputWidth + col] = color;
                    continue;
                }

                if (pointConfidence < zeroingScoresBelow) {
                    colors[row * outputWidth + col] = 0;
                    continue;
                }
                int alphaScaled = (int) Math.min(pointConfidence * 255, maxAlpha);
                int color = (alphaScaled << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
                colors[row * outputWidth + col] = color;
            }
        }
        return colors;
    }

    public int getColorWithAlpha(int color,float ratio){
        int newColor=0;
        int alpha=Math.round(Color.alpha(color)*ratio);
        int r=Color.red(color);
        int g=Color.green(color);
        int b=Color.blue(color);
        newColor=Color.argb(alpha,r,g,b);
        return newColor;
    }


    public Bitmap getBitmapArroundMask(MaskType maskType, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow, int maskColor){
        int[] maskIndexToColor = new int[maskTypes.length];
        Log.d("FritzVisonSegmentResult ", "buildSingleClassMask: "+maskTypes.length);
        for (int i = 0; i < maskTypes.length; i++) {
            MaskType maskTypeToCompare = maskTypes[i];

            if (maskType == maskTypeToCompare) {
                maskIndexToColor[i] = maskColor;
            } else {
                maskIndexToColor[i] = 0;
            }
        }

        int[] colors = getColorAroundMask(maskIndexToColor, maxAlpha, clippingScoresAbove, zeroingScoresBelow);
        Bitmap bitmap = Bitmap.createBitmap(colors, modelOutputSize.getWidth(), modelOutputSize.getHeight(), Bitmap.Config.ARGB_8888);
        return bitmap;

    }

    public int[] getColorAroundMask(int[] maskIndexToColor, int maxAlpha, float clippingScoresAbove, float zeroingScoresBelow){
        int outputWidth = modelOutputSize.getWidth();
        int outputHeight = modelOutputSize.getHeight();
        int[] colors = new int[outputHeight * outputWidth];

        int [][] indexAr=new int[outputHeight][outputWidth];
        int dx[]={0,0,1,-1,-1,-1,1,1};
        int dy[]={1,-1,0,0,-1,1,-1,1};
        for (int row = 0; row < outputHeight; row++) {
            for (int col = 0; col < outputWidth; col++) {
                //indexAr[row][col]=classifications[row][col];
                int maskTypeIndex = classifications[row][col];
                float pointConfidence = confidence[row][col];

                // If the color is transparent, set it and be done.
                if (maskIndexToColor[maskTypeIndex] == 0) {
                    colors[row * outputWidth + col] = 0;
                    continue;
                }

                for(int i=0;i<8;i++){
                    int u=row+dx[i];
                    int v=col+dy[i];
                    if(u>=0 && u<outputHeight && v>=0 && v<outputWidth && classifications[u][v]==0){
                        indexAr[u][v]=1;
                        colors[u*outputWidth+v]= MaskType.PERSON.getColorIdentifier();
                        for(int j=2;j<11;j++) {
                            if (u + dx[i] * j >= 0 && v + dy[i] * j >= 0 && u + dx[i] * j < outputHeight && v + dy[i] * j < outputWidth) {
                                indexAr[u + dx[i] * j][v + dy[i] * j] = 1;
                                colors[(u + dx[i] * j) * outputWidth + (v + dy[i] * j)] = MaskType.PERSON.getColorIdentifier();

//                                if (pointConfidence > clippingScoresAbove) {
//                                    int color = (maxAlpha << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
//                                    colors[(u + dx[i] * j) * outputWidth + (v + dy[i] * j)] = getColorWithAlpha(Color.BLACK, 0.1f);
//                                    continue;
//                                }
//                                if (pointConfidence < zeroingScoresBelow) {
//                                    colors[(u + dx[i] * j) * outputWidth + (v + dy[i] * j)] = getColorWithAlpha(Color.BLACK, 0.1f);;
//                                    continue;
//                                }
//                                int alphaScaled = (int) Math.min(pointConfidence * 255, maxAlpha);
//                                int color = (alphaScaled << ALPHA_SHIFT) | maskIndexToColor[maskTypeIndex] & MASK_RGB;
//                                colors[(u + dx[i] * j) * outputWidth + (v + dy[i] * j)] = getColorWithAlpha(Color.BLACK, 0.1f);
                            }

                        }
                    }
                }
            }
        }
        Bitmap bm=Bitmap.createBitmap(colors,outputWidth,outputHeight, Bitmap.Config.ARGB_8888);
        return colors;
    }

    /**
     * Get a bitmap with all masks drawn.
     *
     * @return a bitmap.
     * @deprecated use
     */
    @Deprecated
    public Bitmap getResultBitmap() {
        return getResultBitmap(targetInferenceSize);
    }

    /**
     * Get a bitmap with all masks drawn scaled to the given size.
     *
     * @param canvasSize - the desired size of the bitmap returned.
     * @return a bitmap scaled to the given size.
     * @deprecated
     */
    @Deprecated
    public Bitmap getResultBitmap(Size canvasSize) {
        // Create a mutable bitmap from the original image
        Bitmap originalBitmap = getOriginalImage().rotateBitmap();
        Bitmap resizedBitmap = BitmapUtils.resize(originalBitmap, canvasSize.getWidth(), canvasSize.getHeight());
        Bitmap mutableBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Creating a canvas to add the mask to the mutable bitmap.
        Canvas canvas = new Canvas(mutableBitmap);

        // Print the mask overlay on the canvas (without the original image)
        drawAllMasks(canvas, DEFAULT_ALPHA_VALUE, canvasSize);

        // return the mutable bitmap to display on an image view.
        return mutableBitmap;
    }

    /**
     * Draw all masks.
     * <p>
     * Mask color overlays will be 25% opacity.
     *
     * @param canvas
     * @deprecated use
     */
    @Deprecated
    public void drawAllMasks(Canvas canvas) {
        drawAllMasks(canvas, DEFAULT_ALPHA_VALUE, targetInferenceSize);
    }

    /**
     * Draw a scaled mask for a specific canvas size.
     *
     * @param canvas
     * @param alpha
     * @param canvasSize
     * @deprecated use
     */
    @Deprecated
    public void drawAllMasks(Canvas canvas, int alpha, Size canvasSize) {
        Bitmap bitmap = createMaskOverlayBitmap(alpha);
        canvas.drawBitmap(bitmap, null, new RectF(0, 0, canvasSize.getWidth(), canvasSize.getHeight()), null);
    }

    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Cut out the masked layer from the original image and create a new bitmap.
     * If no pixels are associated with the mask type, return null.
     * <p>
     * Example:
     * <pre>{@code
     *  Bitmap personBitmap = result.createMaskedBitmap(MaskType.PERSON);
     *  if(person != null) {
     *      canvas.drawBitmap(personBitmap, ...);
     *  }
     * }</pre>
     *
     * @param maskType - the mask to create a bitmap of.
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     * @deprecated use {@link
     */
    public Bitmap createMaskedBitmap(MaskType maskType) {
        return createMaskedBitmap(maskType, 1, confidenceThreshold);
    }

    /**
     * Create a bitmap for the parts of the image that aren't classified.
     *
     * @return a bitmap of MaskType.NONE
     * @deprecated  with MaskType.NONE
     */
    public Bitmap createBackgroundBitmap() {
        return createMaskedBitmap(MaskType.NONE, -1, 0);
    }

    /**
     * Create a bitmap for the masked section of the image.
     * <p>
     * Cut out the masked layer from the original image and create a new bitmap.
     * If no pixels are associated with the mask type, return null.
     * <p>
     * Example:
     * <pre>{@code
     *  Bitmap personBitmap = result.createMaskedBitmap(MaskType.PERSON);
     *  if(person != null) {
     *      canvas.drawBitmap(personBitmap, ...);
     *  }
     * }</pre>
     *
     * @param maskType            - the mask to create a bitmap of.
     * @param clippingScoresAbove - scores above this threshold will set the alpha value as 1
     * @param zeroingScoresBelow  - scores below this threshold will set the alpha value as 0
     * @return an Optional Bitmap for the masked pixels. Will return null if the class was not detected.
     * @deprecated use
     */
    public Bitmap createMaskedBitmap(MaskType maskType, float clippingScoresAbove, float zeroingScoresBelow) {
        int maskToFindIndex = 0;
        for (int i = 0; i < maskTypes.length; i++) {
            if (maskTypes[i].hashCode() == maskType.hashCode()) {
                maskToFindIndex = i;
                break;
            }
        }

        float scaleFactorWidth = ((float) targetInferenceSize.getWidth()) / modelOutputSize.getWidth();
        float scaleFactorHeight = ((float) targetInferenceSize.getHeight()) / modelOutputSize.getHeight();

        float scaledOffsetX = (offsetX == 0) ? 0 : ((float) targetInferenceSize.getWidth()) / offsetX;
        float scaledOffsetY = (offsetY == 0) ? 0 : ((float) targetInferenceSize.getHeight()) / offsetY;

        // Get the bitmap boundaries

        int height = modelOutputSize.getHeight();
        int width = modelOutputSize.getWidth();

        int leftMin = width;
        int rightMax = 0;
        int topMax = 0;
        int bottomMin = height;

        boolean detectedClass = false;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int maskClassIndex = classifications[row][col];
                if (maskToFindIndex != maskClassIndex) {
                    continue;
                }
                leftMin = Math.min(col, leftMin);
                rightMax = Math.max(col, rightMax);
                topMax = Math.max(row, topMax);
                bottomMin = Math.min(row, bottomMin);
                detectedClass = true;
            }
        }

        // If the class was not detected, don't create the bitmap.
        if (!detectedClass) {
            return null;
        }

        RectF bitmapBounds = new RectF(
                leftMin * scaleFactorWidth + scaledOffsetX,
                (topMax + 1) * scaleFactorHeight + scaledOffsetY,
                (rightMax + 1) * scaleFactorWidth + scaledOffsetX,
                bottomMin * scaleFactorHeight + scaledOffsetY);

        // Create a new mutable bitmap with a certain size
        int bitmapWidth = (int) bitmapBounds.right - (int) bitmapBounds.left;
        int bitmapHeight = (int) bitmapBounds.top - (int) bitmapBounds.bottom;
        RectF boxScaled = new RectF(0, 0, 0, 0);

        int[] pixels = new int[bitmapHeight * bitmapWidth];
        Bitmap originalImage = getOriginalImage().rotateBitmap();

        // Copy the pixels from an original image
        for (int row = bottomMin; row <= topMax; row++) {
            for (int col = leftMin; col <= rightMax; col++) {
                int maskClassIndex = classifications[row][col];
                float pointConfidence = classifications[row][col];

                if (maskTypes[maskClassIndex].hashCode() != maskType.hashCode()) {
                    continue;
                }

                boxScaled.left = (int) (col * scaleFactorWidth + scaledOffsetX);
                boxScaled.top = (int) ((row + 1) * scaleFactorHeight + scaledOffsetY);
                boxScaled.right = (int) ((col + 1) * scaleFactorWidth + scaledOffsetX);
                boxScaled.bottom = (int) (row * scaleFactorHeight + scaledOffsetY);

                boolean shouldZero = pointConfidence < zeroingScoresBelow;
                boolean shouldClipAbove = pointConfidence > clippingScoresAbove;

                for (int y = (int) boxScaled.bottom; y < (int) boxScaled.top; y++) {
                    for (int x = (int) boxScaled.left; x < (int) boxScaled.right; x++) {
                        int locX = (int) (x - bitmapBounds.left);
                        int locY = (int) (y - bitmapBounds.bottom);
                        // Get the pixel from the original image and copy it to the new bitmap for the mask
                        int pixelColor = originalImage.getPixel(x, y);

                        if (shouldClipAbove) {
                            pixels[locY * bitmapWidth + locX] = pixelColor;
                            continue;
                        }

                        if (shouldZero) {
                            pixels[locY * bitmapWidth + locX] = 0;
                            continue;
                        }

                        int alphaScaled = (int) Math.min(pointConfidence * 255, 255);
                        int color = (alphaScaled << ALPHA_SHIFT) | pixelColor & MASK_RGB;
                        pixels[locY * bitmapWidth + locX] = color;
                    }
                }
            }
        }

        return Bitmap.createBitmap(pixels, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
    }
}
