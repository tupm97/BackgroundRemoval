package com.example.backgroundremoval.lib;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;

import org.tensorflow.lite.Tensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class VisionSegmentTFLPredictor extends VisionTFLitePredictor<VisionSegmentResult> {

    private static final String TAG = VisionSegmentTFLPredictor.class.getSimpleName();

    private Size inputSize;
    private Size outputSize;

    private int[] intValues;
    private MaskType[] segmentClassifications;
    private VisionSegmentPredictorOptions options;

    private ByteBuffer inputByteBuffer;
    private ByteBuffer outputByteBuffer;

    public VisionSegmentTFLPredictor(SegmentOnDeviceModel segmentOnDeviceModel, VisionSegmentPredictorOptions options) {
        super(segmentOnDeviceModel);
        initializeValues(segmentOnDeviceModel, options);
    }

    private void initializeValues(SegmentOnDeviceModel segmentOnDeviceModel, VisionSegmentPredictorOptions options) {
        interpreter.setNumThreads(options.getNumThreads());
        this.segmentClassifications = setTargetClassifications(segmentOnDeviceModel.getClassifications(), options.getTargetSegments());

//        Log.d(TAG, "initializeValues: MaskType "+options.getTargetSegments());

        Tensor inputTensor = interpreter.getInputTensor(0);
        inputSize = getSizeFromTensor(inputTensor);
        inputByteBuffer = ByteBuffer.allocateDirect(4 * inputTensor.numElements());
        inputByteBuffer.order(ByteOrder.nativeOrder());

        Tensor outputTensor = interpreter.getOutputTensor(0);
        outputSize = inputSize = getSizeFromTensor(outputTensor);

        outputByteBuffer = ByteBuffer.allocateDirect(4 * outputTensor.numElements());
        outputByteBuffer.order(ByteOrder.nativeOrder());

        this.intValues = new int[inputSize.getHeight() * inputSize.getWidth()];
        this.options = options;
    }

    public void setOptions(VisionSegmentPredictorOptions options) {
        this.options = options;
        this.segmentClassifications = setTargetClassifications(this.segmentClassifications, options.getTargetSegments());
        interpreter.setNumThreads(options.getNumThreads());
    }

    /**
     * Identify and create pixel-level masks for all items in visionImage.
     *
     * @param visionImage
     * @return FritzVisionSegmentResult
     */
    @Override
    public VisionSegmentResult predict(VisionImage visionImage) {
        long start = System.nanoTime();
        PreparedImage preparedImage = PreparedImage.create(visionImage, options.getCropAndScaleOption(), inputSize);

        preprocess(preparedImage.getBitmapForModel());
        long preprocessTiming = System.nanoTime() - start;
       // EventTracker.getInstance().trackCustomTiming(ModelEventName.MODEL_PREPROCESS, onDeviceModel, preprocessTiming);

        outputByteBuffer.rewind();
        start = System.nanoTime();
        interpreter.run(inputByteBuffer, outputByteBuffer);
        Log.d(TAG, "model inference took " + Math.floor((System.nanoTime() - start) / 1e6) + "ms to run.");

        start = System.nanoTime();
        VisionSegmentResult result = postprocess(visionImage, preparedImage);
        long postprocessTiming = System.nanoTime() - start;

        return result;
    }

    private MaskType[] setTargetClassifications(MaskType[] classifications, List<MaskType> targetSegments) {
        // if no target segments set, then use the default
        if (targetSegments == null) {
            return classifications;
        }

        // Filter out the classes outside of the target segments
        for (int i = 0; i < classifications.length; i++) {
            MaskType maskType = classifications[i];
            if (!targetSegments.contains(maskType)) {
                classifications[i] = MaskType.NONE;
            }
        }

        return classifications;
    }


    // convert input Bitmap to Byte[]
    // take pixel from Bitmap, use method getPixels
    private void preprocess(Bitmap bitmap) {
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        inputByteBuffer.rewind();
        int height = inputSize.getHeight();
        int width = inputSize.getWidth();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixel = intValues[row * width + col];

                float blue = (float) (pixel & 0xFF) / 255f - 0.5f;
                float green = (float) ((pixel >> 8) & 0xFF) / 255f - 0.5f;
                float red = (float) ((pixel >> 16) & 0xFF) / 255f - 0.5f;

                inputByteBuffer.putFloat(red);
                inputByteBuffer.putFloat(green);
                inputByteBuffer.putFloat(blue);
            }
        }
        Log.d(TAG, "preprocess: pixel "+intValues[0]);
    }

    private VisionSegmentResult postprocess(VisionImage visionImage, PreparedImage preparedImage) {
        int[][] classifications = new int[outputSize.getHeight()][outputSize.getWidth()];
        float[][] confidence = new float[outputSize.getHeight()][outputSize.getWidth()];

        int height = outputSize.getHeight();
        int width = outputSize.getWidth();

        for (int row = 0; row < height; row++) {
            int rowOffset = row * width * segmentClassifications.length;

            for (int col = 0; col < width; col++) {
                int maxClassProbIndex = 0;
                float maxClassProbValue = 0;

                int colOffset = col * segmentClassifications.length;
                int offset = rowOffset + colOffset;

                for (int classIndex = 0; classIndex < segmentClassifications.length; classIndex++) {
                    float classProb = outputByteBuffer.getFloat((offset + classIndex) * 4);

                    // Arg max
                    if (classProb > maxClassProbValue) {
                        maxClassProbIndex = classIndex;
                        maxClassProbValue = classProb;
                    }
                }
                classifications[row][col] = maxClassProbIndex;
                confidence[row][col] = maxClassProbValue;
            }
        }

        return new VisionSegmentResult(
                visionImage, preparedImage,
                options, segmentClassifications,
                preparedImage.getTargetInferenceSize(),
                outputSize,
                preparedImage.getOffsetX(),
                preparedImage.getOffsetY(),
                classifications, confidence);
    }
}
