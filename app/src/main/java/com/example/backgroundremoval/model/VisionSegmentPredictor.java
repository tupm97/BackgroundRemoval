package com.example.backgroundremoval.model;

public class VisionSegmentPredictor extends VisionSegmentTFLPredictor{

    public VisionSegmentPredictor(SegmentOnDeviceModel segmentOnDeviceModel, VisionSegmentPredictorOptions options) {
        super(segmentOnDeviceModel, options);
    }
}
