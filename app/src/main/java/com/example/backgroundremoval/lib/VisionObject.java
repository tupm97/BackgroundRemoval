package com.example.backgroundremoval.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Locale;

public class VisionObject {

    private static final String TAG = VisionObject.class.getSimpleName();

    // These are used for drawing the text relative to the bounding box.
    private static final int LEFT_OFFSET = 24;
    private static final int TOP_OFFSET = 24;

    private VisionLabel visionLabel;
    private RectF boundingBox;

    public VisionObject(String text, float confidence, RectF boundingBox) {
        this.visionLabel = new VisionLabel(text, confidence);
        this.boundingBox = boundingBox;
    }

    public VisionObject(VisionLabel visionLabel, RectF boundingBox) {
        this.visionLabel = visionLabel;
        this.boundingBox = boundingBox;
    }

    public VisionLabel getVisionLabel() {
        return visionLabel;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    /**
     * Draw the bounding boxes to the canvas exactly as they are.
     *
     * @param context
     * @param canvas
     * @deprecated This method will be removed in the next major version update.
     */
    public void drawOnCanvas(Context context, Canvas canvas) {
        drawOnCanvas(context, canvas, 1.0f, 1.0f);
    }

    /**
     * Draw on the canvas and scale the boxes to fit a new screen size.
     * <p>
     * This is used if you want to run object detection on a small preview sized screen and then
     * fit the boxes for a larger screen size.
     * <p>
     * In order to do this, you should calculate the scale factors before calling this method:
     *
     * <pre>{@code
     *
     * float scaleFactorWidth = ((float) desiredScreenSize.getWidth()) / originalBitmapUsedForPrediction.getWidth();
     * float scaleFactorHeight = ((float) desiredScreenSize.getHeight()) / originalBitmapUsedForPrediction.getHeight();
     *
     * object.drawOnCanvas(context, canvas, scaleFactorWidth, scaleFactorHeight)
     *
     * }</pre>
     *
     * @param context
     * @param canvas
     * @param scaleFactorWidth  - factor to scale up the box widths
     * @param scaleFactorHeight - factor to scale up the box heights
     * @deprecated This method will be removed in the next major version update.
     */
    public void drawOnCanvas(Context context, Canvas canvas, float scaleFactorWidth, float scaleFactorHeight) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);

        RectF boxScaled = new RectF(boundingBox.left * scaleFactorWidth, boundingBox.top * scaleFactorHeight, boundingBox.right * scaleFactorWidth, boundingBox.bottom * scaleFactorHeight);

        canvas.drawRect(boxScaled, paint);

        BorderedText borderedText = BorderedText.createDefault(context);
        final String labelString = String.format(Locale.ENGLISH, "%s %.2f", visionLabel.getText(), visionLabel.getConfidence());
        borderedText.drawText(canvas, boxScaled.left + LEFT_OFFSET, boxScaled.top + TOP_OFFSET, labelString);
    }

    /**
     * Draw the bounding box and associated text on the canvas.
     *
     * @param canvas - the canvas to draw on
     */
    public void draw(Canvas canvas) {
        String text = String.format(Locale.ENGLISH, "%s %.2f", visionLabel.getText(), visionLabel.getConfidence());
        canvas.drawRect(boundingBox, DrawingUtils.DEFAULT_PAINT);
        canvas.drawText(text, boundingBox.left + LEFT_OFFSET, boundingBox.top + TOP_OFFSET, DrawingUtils.DEFAULT_TEXT_PAINT);
    }
}
