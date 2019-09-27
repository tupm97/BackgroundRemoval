package com.example.backgroundremoval.model;

import android.util.Size;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

public abstract class VisionTFLitePredictor<T> extends VisionPredictorBase {

    private static final int DEFAULT_HEIGHT_IDX = 1;
    private static final int DEFAULT_WIDTH_IDX = 2;

    protected OnDeviceModel onDeviceModel;
    protected TFLiteInterpreter interpreter;


    public VisionTFLitePredictor(OnDeviceModel onDeviceModel) {
        this(onDeviceModel, new Interpreter.Options());
    }

    public VisionTFLitePredictor(OnDeviceModel onDeviceModel, Interpreter.Options interpreterOptions) {
        this.onDeviceModel = onDeviceModel;
        this.interpreter = new TFLiteInterpreter(onDeviceModel, interpreterOptions);
    }

    public void close() {
        this.interpreter.close();
    }

    protected Size getSizeFromTensor(Tensor tensor) {
        int[] inputShape = tensor.shape();
        int inputHeight = inputShape[DEFAULT_HEIGHT_IDX];
        int inputWidth = inputShape[DEFAULT_WIDTH_IDX];
        return new Size(inputWidth, inputHeight);
    }
}
