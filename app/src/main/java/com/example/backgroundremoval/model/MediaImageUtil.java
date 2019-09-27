package com.example.backgroundremoval.model;

import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

public class MediaImageUtil {

    public static final String TAG = MediaImageUtil.class.getSimpleName();
    // Don't actually do this
//    static RenderScript rs;
//
//    static {
//        rs = RenderScript.create(Fritz.getAppContext());
//    }

    /**
     * Utility method to compute the allocated size in bytes of a YUV420SP image
     * of the given dimensions.
     */
    public static int getYUVByteSize(final int width, final int height) {
        // The luminance plane requires 1 byte per pixel.
        final int ySize = width * height;

        // The UV plane works on 2x2 blocks, so dimensions with odd size must be rounded up.
        // Each 2x2 block takes 2 bytes to encode, one each for U and V.
        final int uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2;

        return ySize + uvSize;
    }

    // This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
    // are normalized to eight bits.
    static final int kMaxChannelValue = 262143;

    public static void convertYUV420SPToARGB8888(
            byte[] input,
            int width,
            int height,
            int[] output) {

        // Java implementation of YUV420SP to ARGB8888 converting
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
            int u = 0;
            int v = 0;

            for (int i = 0; i < width; i++, yp++) {
                int y = 0xff & input[yp];
                if ((i & 1) == 0) {
                    v = 0xff & input[uvp++];
                    u = 0xff & input[uvp++];
                }

                output[yp] = YUV2RGB(y, u, v);
            }
        }
    }

    private static int YUV2RGB(int y, int u, int v) {
        // Adjust and check YUV values
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = r > kMaxChannelValue ? kMaxChannelValue : (r < 0 ? 0 : r);
        g = g > kMaxChannelValue ? kMaxChannelValue : (g < 0 ? 0 : g);
        b = b > kMaxChannelValue ? kMaxChannelValue : (b < 0 ? 0 : b);

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }

//    public static Bitmap YUV_420_888_toRGB(Image image, int width, int height) {
//        // Get the three image planes
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer buffer = planes[0].getBuffer();
//        byte[] y = new byte[buffer.remaining()];
//        buffer.get(y);
//
//        buffer = planes[1].getBuffer();
//        byte[] u = new byte[buffer.remaining()];
//        buffer.get(u);
//
//        buffer = planes[2].getBuffer();
//        byte[] v = new byte[buffer.remaining()];
//        buffer.get(v);
//
//        // get the relevant RowStrides and PixelStrides
//        // (we know from documentation that PixelStride is 1 for y)
//        int yRowStride = planes[0].getRowStride();
//        int uvRowStride = planes[1].getRowStride();  // we know from   documentation that RowStride is the same for u and v.
//        int uvPixelStride = planes[1].getPixelStride();  // we know from   documentation that PixelStride is the same for u and v.
//
//
//        // rs creation just for demo. Create rs just once in onCreate and use it again.
//        ScriptC_yuv420888 mYuv420 = new ScriptC_yuv420888(rs);
//
//        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
//        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
//        Type.Builder typeUcharY = new Type.Builder(rs, Element.U8(rs));
//        typeUcharY.setX(yRowStride).setY(height);
//        Allocation yAlloc = Allocation.createTyped(rs, typeUcharY.create());
//        yAlloc.copy1DRangeFrom(0, y.length, y);
//        mYuv420.set_ypsIn(yAlloc);
//
//        Type.Builder typeUcharUV = new Type.Builder(rs, Element.U8(rs));
//        typeUcharUV.setX(u.length);
//        Allocation uAlloc = Allocation.createTyped(rs, typeUcharUV.create());
//        uAlloc.copyFrom(u);
//        mYuv420.set_uIn(uAlloc);
//
//        Allocation vAlloc = Allocation.createTyped(rs, typeUcharUV.create());
//        vAlloc.copyFrom(v);
//        mYuv420.set_vIn(vAlloc);
//
//        // handover parameters
//        mYuv420.set_picWidth(width);
//        mYuv420.set_uvRowStride(uvRowStride);
//        mYuv420.set_uvPixelStride(uvPixelStride);
//
//        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Allocation outAlloc = Allocation.createFromBitmap(rs, outBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
//
//        Script.LaunchOptions lo = new Script.LaunchOptions();
//        lo.setX(0, width);  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
//        lo.setY(0, height);
//
//        mYuv420.forEach_doConvert(outAlloc, lo);
//        outAlloc.copyTo(outBitmap);
//
//        return outBitmap;
//    }

    public static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            for (; pos < ySize; pos += width) {
                yBuffer.get(nv21, pos, width);
                yBuffer.position(yBuffer.position() + rowStride - width); // skip
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            vBuffer.put(1, (byte) 0);
            if (uBuffer.get(0) == 0) {
                vBuffer.put(1, (byte) 255);
                if (uBuffer.get(0) == 255) {
                    vBuffer.put(1, savePixel);
                    vBuffer.get(nv21, ySize, uvSize);

                    return nv21; // shortcut
                }
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                nv21[pos++] = vBuffer.get(col + row * rowStride);
                nv21[pos++] = uBuffer.get(col + row * rowStride);
            }
        }

        return nv21;
    }

    public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {

        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uv_offset = pUV + (i >> 1) * uvPixelStride;

                out[yp++] = YUV2RGB(
                        0xff & yData[pY + i],
                        0xff & uData[uv_offset],
                        0xff & vData[uv_offset]);
            }
        }
    }

    /**
     * Returns a transformation matrix from one reference frame into another.
     * Handles cropping (if maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth            Width of source frame.
     * @param srcHeight           Height of source frame.
     * @param dstWidth            Width of destination frame.
     * @param dstHeight           Height of destination frame.
     * @param applyRotation       Amount of rotation to apply from one frame to another.
     *                            Must be a multiple of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     *                            cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation,
            final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                Log.w(TAG, "Rotation of " + applyRotation + " % 90 != 0");
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }
}
