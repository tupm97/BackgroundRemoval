package com.example.backgroundremoval.lib;

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
