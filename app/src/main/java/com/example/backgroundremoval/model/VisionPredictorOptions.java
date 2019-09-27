package com.example.backgroundremoval.model;

public class VisionPredictorOptions {

    public VisionCropAndScale cropAndScaleOption;

    public VisionPredictorOptions() {
    }

    public VisionPredictorOptions(VisionCropAndScale cropAndScaleOption) {
        this.cropAndScaleOption = cropAndScaleOption;
    }

    public VisionCropAndScale getCropAndScaleOption() {
        return cropAndScaleOption;
    }
}
