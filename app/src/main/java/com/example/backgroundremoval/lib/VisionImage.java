package com.example.backgroundremoval.lib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.Image;
import android.util.Size;
import android.view.Surface;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisionImage implements Serializable {

//    private static final String TAG = FritzVisionImage.class.getSimpleName();
    private static final Map<Integer, Integer> ORIENTATIONS = new HashMap<>();

    private static BitmapFactory.Options opts = new BitmapFactory.Options();

    static {
        ORIENTATIONS.put(Surface.ROTATION_0, 90);
        ORIENTATIONS.put(Surface.ROTATION_90, 0);
        ORIENTATIONS.put(Surface.ROTATION_180, 270);
        ORIENTATIONS.put(Surface.ROTATION_270, 180);
        opts.inSampleSize = 1;
    }

    /**
     * Convert a bitmap to a FritzVisionImage.
     *
     * @param bitmap the bitmap to convert.
     * @return a FritzVisionImage object.
     */
    public static VisionImage fromBitmap(Bitmap bitmap) {
        return new VisionImage(bitmap);
    }

    public static VisionImage fromBitmap(Bitmap bitmap, int rotation) {
        VisionImage visionImage = new VisionImage(bitmap, rotation);
        return visionImage;
    }

    /**
     * Overlay a bitmap onto the original image.
     *
     * @param image - the image to superimpose on the original.
     * @return the original image with the mask overlay.
     */
    public Bitmap overlay(Bitmap image) {
        Bitmap sourceBitmap = rotateBitmap();

        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(image, null, new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        return output;
    }

    /**
     * Overlay the Object Detection result onto the original image.
     *
     * @param visionObject - the object to draw.
     * @return a bitmap with the object on the original image.
     */
    public Bitmap overlayBoundingBox(VisionObject visionObject) {

        Bitmap sourceBitmap = rotateBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        visionObject.draw(canvas);

        return output;
    }

    /**
     * Overlay the Object Detection results onto the original image.
     *
     * @param visionObjects - the objects to draw.
     * @return a bitmap with the objects on the original image.
     */
    public Bitmap overlayBoundingBoxes(List<VisionObject> visionObjects) {
        Bitmap sourceBitmap = rotateBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);

        for (VisionObject visionObject : visionObjects) {
            visionObject.draw(canvas);
        }

        return output;
    }

    /**
     * Overlay the Pose estimation results onto the original image.
     *
     * @param pose - the pose to draw.
     * @return a bitmap with the pose on the original image.
     */
    public Bitmap overlaySkeleton(Pose pose) {
        Bitmap sourceBitmap = rotateBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);
        pose.draw(canvas);

        return output;

    }

    /**
     * Overlay the Pose estimation results onto the original image.
     *
     * @param poses - the poses to draw.
     * @return a bitmap with the poses on the original image.
     */
    public Bitmap overlaySkeletons(List<Pose> poses) {
        Bitmap sourceBitmap = rotateBitmap();
        Bitmap output = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        Canvas canvas = new Canvas(output);

        for (Pose pose : poses) {
            pose.draw(canvas);
        }

        return output;
    }


    /**
     * Crop the masked section from the image.
     * <p>
     * The output will have the same dimensions as the original image.
     *
     * @return a bitmap of the cropped section.
     */
    public Bitmap mask(Bitmap maskedBitmap) {
        return mask(maskedBitmap, false);
    }

    /**
     * Crop the masked section from the image.
     * <p>
     * Pass in an alpha mask of the section you'd like th crop from the original image.
     * The output will have the same dimensions as the original image if trim is false.
     *
     * @param mask - the alpha mask to crop from the image.
     * @param trim - trim the extra transparent pixels from the result.
     * @return a bitmap of the cropped section.
     */
    public Bitmap mask(Bitmap mask, boolean trim) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Bitmap sourceBitmap = rotateBitmap();
//        Bitmap sourceBitmap=makeTransparent(sourceBitmap1);

        Bitmap output = Bitmap.createBitmap(sourceBitmap.getWidth(),
                sourceBitmap.getHeight(), Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(mask, null, new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        canvas.drawBitmap(sourceBitmap, 0, 0, paint);

        if (trim) {
            return trimBounds(output);
        }

        return output;
    }
    public Bitmap mask2(Bitmap mask, boolean trim) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Bitmap sourceBitmap1 = rotateBitmap();
        Bitmap sourceBitmap=makeTransparent(sourceBitmap1);

        Bitmap output = Bitmap.createBitmap(sourceBitmap.getWidth(),
                sourceBitmap.getHeight(), Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(mask, null, new RectF(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), null);
        canvas.drawBitmap(sourceBitmap, 0, 0, paint);

        if (trim) {
            return trimBounds(output);
        }

        return output;
    }

    private Bitmap trimBounds(Bitmap source) {

        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int leftMin = width;
        int rightMax = 0;
        int topMax = 0;
        int bottomMin = height;

        boolean detected = false;

        for (int row = 0; row < source.getHeight(); row++) {
            for (int col = 0; col < source.getWidth(); col++) {
                int color = pixels[row * source.getWidth() + col];
                if (color == Color.TRANSPARENT) {
                    continue;
                }
                leftMin = Math.min(col, leftMin);
                rightMax = Math.max(col, rightMax);
                topMax = Math.max(row, topMax);
                bottomMin = Math.min(row, bottomMin);
                detected = true;
            }
        }

        // If the class was not detected, don't create the source.
        if (!detected) {
            return null;
        }

        RectF sourceBounds = new RectF(
                leftMin,
                topMax,
                rightMax,
                bottomMin
        );

        int boundWidth = rightMax - leftMin;
        int boundHeight = topMax - bottomMin;

        return Bitmap.createBitmap(source, (int) sourceBounds.left, (int) sourceBounds.bottom, boundWidth, boundHeight);
    }



    // Convert transparentColor to be transparent in a Bitmap.
    public static Bitmap makeTransparent(Bitmap bit) {
        int width =  bit.getWidth();
        int height = bit.getHeight();
        Bitmap myBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        int [] allpixels = new int [ myBitmap.getHeight()*myBitmap.getWidth()];
        bit.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(),myBitmap.getHeight());
        myBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);

        for(int i =0; i<myBitmap.getHeight()*myBitmap.getWidth();i++){
            int color=allpixels[i];
            int r=Color.red(color);
            int g=Color.green(color);
            int b=Color.blue(color);
            int alpha=Math.round(Color.alpha(color)*0.6f);
            allpixels[i]=Color.argb(alpha,r,g,b);
        }
        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        return myBitmap;
    }

    /**
     * Blend a bitmap on top of the original image.
     *
     * @param maskBitmap
     * @param blendMode
     * @return
     */
    public Bitmap blend(Bitmap maskBitmap, BlendMode blendMode) {
        Bitmap bitmap = rotateBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap resizedMaskBitmap = BitmapUtils.resize(maskBitmap, width, height);

        int[] maskColors = new int[width * height];
        resizedMaskBitmap.getPixels(maskColors, 0, width, 0, 0, width, height);

        int[] originalBitmapColors = new int[width * height];
        bitmap.getPixels(originalBitmapColors, 0, width, 0, 0, width, height);

        BlendComposer composer = getComposer(blendMode, width, height, maskColors, originalBitmapColors);
        int[] result = composer.compose();

        Bitmap resultBitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
        return resultBitmap;
    }

    private BlendComposer getComposer(BlendMode blendMode, int width, int height, int[] maskColors, int[] imgColors) {
        if (blendMode.getType() == BlendModeType.HUE) {
            return new HueComposer(width, height, maskColors, imgColors);
        }

        if (blendMode.getType() == BlendModeType.COLOR) {
            return new ColorComposer(width, height, maskColors, imgColors);
        }

        return new SoftLightComposer(width, height, imgColors, maskColors);
    }

    /**
     * Convert from a media image to a bitmap.
     * <p>
     * TODO: Need to test this out with other formats other than YUV_420.
     * https://developer.android.com/reference/android/media/Image
     *
     * @param image
     * @param rotation - rotation depending on the camera and the device rotation. See {@link VisionOrientation#getImageRotationFromCamera(Activity, String)}
     * @return FritzVisionImage
     */
    public static VisionImage fromMediaImage(Image image, int rotation) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (image.getFormat() == ImageFormat.YUV_420_888) {
//            Bitmap rgbBitmap = MediaImageUtil.YUV_420_888_toRGB(image, width, height);
            Bitmap rgbBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            int[] rgbBytes = new int[image.getWidth() * image.getHeight()];
            final Image.Plane[] planes = image.getPlanes();
            byte[][] yuvBytes = new byte[3][];
            fillBytes(planes, yuvBytes);

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            MediaImageUtil.convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    image.getWidth(),
                    image.getHeight(),
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    rgbBytes);
            rgbBitmap.setPixels(rgbBytes, 0, width, 0, 0, width, height);
            return new VisionImage(rgbBitmap, rotation);
        }

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return new VisionImage(bitmapImage, rotation);
    }


    private static void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    /**
     * FritzVisionImage instance.
     */

    // Immutable bitmap
    private Bitmap bitmap;
    private int rotation;

    private VisionImage(Bitmap bitmap) {
        this(bitmap, 0);
    }

    private VisionImage(Bitmap bitmap, int rotation) {
        this.bitmap = bitmap;
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    /**
     * Get the rotated bitmap.
     *
     * @return the rotated bitmap
     */
    public Bitmap rotateBitmap() {
        return BitmapUtils.rotate(bitmap, rotation);
    }

    /**
     * Get the bitmap before any rotation is applied.
     *
     * @return the bitmap.
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    public Size getRotatedBitmapDimensions() {
        if (rotation == CameraRotation.DEGREES_90.getDegrees() || rotation == CameraRotation.DEGREES_270.getDegrees()) {
            return new Size(bitmap.getHeight(), bitmap.getWidth());
        }

        return new Size(bitmap.getWidth(), bitmap.getHeight());
    }
}
