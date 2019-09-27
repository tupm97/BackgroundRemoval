package com.example.backgroundremoval.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class BitmapUtils {

    private static final String TAG = BitmapUtils.class.getSimpleName();

    /**
     * Crop a center square in the image.
     * <p>
     * Uses the larger of the height or width to create the cropped square image.
     * *
     *
     * @return Return the newly cropped bitmap (centered)
     */
    public static Bitmap centerCropSquare(Bitmap bitmap) {
        int imgSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        bitmap = BitmapUtils.centerCrop(bitmap, imgSize, imgSize);
        return bitmap;
    }

    /**
     * Resize the Bitmap for the underlying model.
     * <p>
     * Warning: this may change the aspect ratio of the image. If you'd like to maintain aspect,
     * use {@link #scale(Bitmap, int, int)}
     *
     * @param width
     * @param height
     * @return the newly resized bitmap.
     */
    public static Bitmap resize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * Rotate the bitmap for the model.
     *
     * @param degrees
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

//    public static Bitmap rotateWithRs(Bitmap bitmap, int rotation) {
//        long start = System.nanoTime();
//        if (rotation == 0F) return bitmap;
//
//        rotatorScript.set_inWidth(bitmap.getWidth());
//        rotatorScript.set_inHeight(bitmap.getHeight());
//        Allocation sourceAllocation = Allocation.createFromBitmap(rs, bitmap,
//                Allocation.MipmapControl.MIPMAP_NONE,
//                Allocation.USAGE_SCRIPT);
//        rotatorScript.set_inImage(sourceAllocation);
//
//        Bitmap.Config config = bitmap.getConfig();
//
//        switch (rotation) {
//            case 90: {
//                Bitmap target = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), config);
//                final Allocation targetAllocation = Allocation.createFromBitmap(rs, target,
//                        Allocation.MipmapControl.MIPMAP_NONE,
//                        Allocation.USAGE_SCRIPT);
//
//                rotatorScript.forEach_rotate_90_clockwise(targetAllocation, targetAllocation);
//                targetAllocation.copyTo(target);
//                Log.d(TAG, "Rotate took " + (System.nanoTime() - start)/1e6 + "ms");
//                return target;
//            }
//            case 180: {
//                Bitmap target = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
//                final Allocation targetAllocation = Allocation.createFromBitmap(rs, target,
//                        Allocation.MipmapControl.MIPMAP_NONE,
//                        Allocation.USAGE_SCRIPT);
//
//                rotatorScript.forEach_rotate_180(targetAllocation, targetAllocation);
//                targetAllocation.copyTo(target);
//                Log.d(TAG, "Rotate took " + (System.nanoTime() - start)/1e6 + "ms");
//                return target;
//            }
//            case 270: {
//                Bitmap target = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), config);
//                final Allocation targetAllocation = Allocation.createFromBitmap(rs, target,
//                        Allocation.MipmapControl.MIPMAP_NONE,
//                        Allocation.USAGE_SCRIPT);
//
//                rotatorScript.forEach_rotate_270_clockwise(targetAllocation, targetAllocation);
//                targetAllocation.copyTo(target);
//                Log.d(TAG, "Rotate took " + (System.nanoTime() - start)/1e6 + "ms");
//                return target;
//            }
//            default:
//                throw new IllegalArgumentException("rotateClockwise() only supports 90 degree increments");
//        }
//    }

    /**
     * Scale the image while maintaining aspect ratio.
     *
     * @param targetWidth
     * @param targetHeight
     */
    public static Bitmap scale(Bitmap bitmap, int targetWidth, int targetHeight) {
        final Matrix matrix = new Matrix();

        // maintain the aspect ratio
        final float scaleFactorWidth = (float) targetWidth / bitmap.getWidth();
        final float scaleFactorHeight = (float) targetHeight / bitmap.getHeight();
        float maxScale = Math.max(scaleFactorWidth, scaleFactorHeight);

        matrix.postScale(maxScale, maxScale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    /**
     * Draw the image onto a canvas.
     *
     * @param canvas
     */
    public static void drawOnCanvas(Bitmap bitmap, Canvas canvas) {
        canvas.drawBitmap(bitmap, new Matrix(), new Paint());
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the hinted dimensions call for it.
     * Does not crop to fit the hinted dimensions.
     *
     * @param src an encoded image
     * @param w   hint width in px
     * @param h   hint height in px
     * @return a decoded Bitmap that is not exactly sized to the hinted dimensions.
     */
    public static Bitmap decodeByteArray(byte[] src, int w, int h) {
        try {
            // calculate sample size based on w/h
            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(src, 0, src.length, opts);
            if (opts.mCancel || opts.outWidth == -1 || opts.outHeight == -1) {
                return null;
            }
            opts.inSampleSize = Math.min(opts.outWidth / w, opts.outHeight / h);
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(src, 0, src.length, opts);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the desired dimensions call for it.
     * Also applies a center-crop a la {@link androidx.appcompat.widget.ImageView.ScaleType#CENTER_CROP}.
     *
     * @param src an encoded image
     * @param w   desired width in px
     * @param h   desired height in px
     * @return an exactly-sized decoded Bitmap that is center-cropped.
     */
    public static Bitmap decodeByteArrayWithCenterCrop(byte[] src, int w, int h) {
        try {
            final Bitmap decoded = decodeByteArray(src, w, h);
            return centerCrop(decoded, w, h);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Returns a new Bitmap copy with a center-crop effect a la
     * {@link androidx.appcompat.widget.ImageView.ScaleType#CENTER_CROP}. May return the input bitmap if no
     * scaling is necessary.
     *
     * @param src original bitmap of any size
     * @param w   desired width in px
     * @param h   desired height in px
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    public static Bitmap centerCrop(final Bitmap src, final int w, final int h) {
        return crop(src, w, h, 0.5f, 0.5f);
    }

    /**
     * Returns a new Bitmap copy with a crop effect depending on the crop anchor given. 0.5f is like
     * {@link androidx.appcompat.widget.ImageView.ScaleType#CENTER_CROP}. The crop anchor will be be nudged
     * so the entire cropped bitmap will fit inside the src. May return the input bitmap if no
     * scaling is necessary.
     * <p>
     * <p>
     * Example of changing verticalCenterPercent:
     * _________            _________
     * |         |          |         |
     * |         |          |_________|
     * |         |          |         |/___0.3f
     * |---------|          |_________|\
     * |         |<---0.5f  |         |
     * |---------|          |         |
     * |         |          |         |
     * |         |          |         |
     * |_________|          |_________|
     *
     * @param src                     original bitmap of any size
     * @param w                       desired width in px
     * @param h                       desired height in px
     * @param horizontalCenterPercent determines which part of the src to crop from. Range from 0
     *                                .0f to 1.0f. The value determines which part of the src
     *                                maps to the horizontal center of the resulting bitmap.
     * @param verticalCenterPercent   determines which part of the src to crop from. Range from 0
     *                                .0f to 1.0f. The value determines which part of the src maps
     *                                to the vertical center of the resulting bitmap.
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    public static Bitmap crop(final Bitmap src, final int w, final int h,
                              final float horizontalCenterPercent, final float verticalCenterPercent) {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0
                || verticalCenterPercent > 1) {
            throw new IllegalArgumentException(
                    "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                            + "1.0f, inclusive.");
        }
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src;
        }
        final Matrix m = new Matrix();
        final float scale = Math.max(
                (float) w / srcWidth,
                (float) h / srcHeight);
        m.setScale(scale, scale);
        final int srcCroppedW, srcCroppedH;
        int srcX, srcY;
        srcCroppedW = Math.round(w / scale);
        srcCroppedH = Math.round(h / scale);
        srcX = (int) (srcWidth * horizontalCenterPercent - srcCroppedW / 2);
        srcY = (int) (srcHeight * verticalCenterPercent - srcCroppedH / 2);
        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);
        final Bitmap cropped = Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */);
        return cropped;
    }
}
