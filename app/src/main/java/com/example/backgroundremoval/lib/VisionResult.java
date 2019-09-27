package com.example.backgroundremoval.lib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Size;

public class VisionResult {

    protected VisionImage originalImage;

    /**
     * Build a result class without the original image.
     *
     * TODO: Change result classes to deprecate access to the original image.
     */
    public VisionResult() {
        this.originalImage = null;
    }

    public VisionResult(VisionImage visionImage) {
        this.originalImage = visionImage;
    }

    /**
     * Get the original image passed to the predictor.
     *
     * @return FritzVisionImage
     */
    public VisionImage getOriginalImage() {
        return originalImage;
    }

    /**
     * Draw the original image passed to the predictor on the canvas.
     *
     * @param canvas
     */
    public void drawVisionImage(Canvas canvas) {
        BitmapUtils.drawOnCanvas(originalImage.rotateBitmap(), canvas);
    }

    public void drawVisionImage(Canvas canvas, Size canvasSize) {
        Bitmap bitmap = BitmapUtils.resize(originalImage.rotateBitmap(), canvasSize.getWidth(), canvasSize.getHeight());
        BitmapUtils.drawOnCanvas(bitmap, canvas);
    }
}
