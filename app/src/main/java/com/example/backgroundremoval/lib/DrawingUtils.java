package com.example.backgroundremoval.lib;

import android.graphics.Color;
import android.graphics.Paint;

public class DrawingUtils {

    public static final Paint DEFAULT_PAINT = buildDefaultBoundingBoxPaint();
    public static final Paint DEFAULT_TEXT_PAINT = buildDefaultTextPaint();

    private static Paint buildDefaultBoundingBoxPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);
        return paint;
    }

    private static Paint buildDefaultTextPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setTextSize(24);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }
}
