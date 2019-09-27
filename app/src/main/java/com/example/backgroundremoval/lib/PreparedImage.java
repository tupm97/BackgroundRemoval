package com.example.backgroundremoval.lib;

import android.graphics.Bitmap;
import android.util.Size;

public class PreparedImage {

    public static PreparedImage create(VisionImage visionImage, VisionCropAndScale cropAndScaleOption, Size modelInputSize) {
        Bitmap bitmap = visionImage.getBitmap();

        // For CENTER CROP, crop the original image for a square center and then resize it for model inference
        if (cropAndScaleOption == VisionCropAndScale.CENTER_CROP) {
            // Center crop first
            Bitmap croppedBitmap = BitmapUtils.centerCropSquare(bitmap);

            // Calculate the cropped bitmap size.
            // This is what we'll scale the result back to.
            int minDimen = Math.min(bitmap.getWidth(), bitmap.getHeight());
            Size croppedBitmapSize = new Size(minDimen, minDimen);

            Bitmap rotatedBitmap = BitmapUtils.rotate(croppedBitmap, visionImage.getRotation());
            Size rotatedDimensions = visionImage.getRotatedBitmapDimensions();

            // Calculate the offset from the original image
            int offsetX = (rotatedDimensions.getWidth() - minDimen) / 2;
            int offsetY = (rotatedDimensions.getHeight() - minDimen) / 2;

            // Return square image resized to the model input size
            Bitmap resizedBitmap = BitmapUtils.resize(rotatedBitmap, modelInputSize.getWidth(), modelInputSize.getHeight());
            return new PreparedImage(resizedBitmap, croppedBitmapSize, offsetX, offsetY);
        }

        // For SCALE_TO_FIT, no offsets bc it covers the original image & the cropped size is just the original size.
        Bitmap scaledBitmap = BitmapUtils.scale(bitmap, modelInputSize.getWidth(), modelInputSize.getHeight());
        Bitmap rotatedBitmap = BitmapUtils.rotate(scaledBitmap, visionImage.getRotation());
        Bitmap resizedBitmap = BitmapUtils.resize(rotatedBitmap, modelInputSize.getWidth(), modelInputSize.getHeight());
        return new PreparedImage(resizedBitmap, getRotatedSizeOfImage(bitmap, visionImage.getRotation()), 0, 0);
    }

    private static Size getRotatedSizeOfImage(Bitmap bitmap, int rotation) {
        if (rotation == CameraRotation.DEGREES_90.getDegrees() || rotation == CameraRotation.DEGREES_270.getDegrees()) {
            return new Size(bitmap.getHeight(), bitmap.getWidth());
        }

        return new Size(bitmap.getWidth(), bitmap.getHeight());
    }

    private Bitmap bitmapForModel;
    private Size targetInferenceSize;
    private int offsetX;
    private int offsetY;

    public PreparedImage(Bitmap bitmapForModel, Size targetInferenceSize, int offsetX, int offsetY) {
        this.bitmapForModel = bitmapForModel;
        this.targetInferenceSize = targetInferenceSize;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Offset (X) helps calculate where to find the target inference area on the original image.
     *
     * For center cropping, the offset to the target inference area.
     * For Scale To Fit, this is 0
     * @return an int of the offset (from the left of the original image)
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Offset (Y) helps calculate where to find the target inference area on the original image.
     *
     * For center cropping, the offset to the target inference area.
     * For Scale To Fit, this is 0
     * @return an int of the offset (from the top of the original image)
     */
    public int getOffsetY() {
        return offsetY;
    }

    public Bitmap getBitmapForModel() {
        return bitmapForModel;
    }

    /**
     * The size of the original image which the model runs inference on.
     *
     * Examples:
     * For Center Crop, the model covers the center prt of the bitmap.
     * For Scale to Fit, model covers the entire bitmap.
     * @return the size of the image that the model covers.
     */
    public Size getTargetInferenceSize() {
        return targetInferenceSize;
    }


}
