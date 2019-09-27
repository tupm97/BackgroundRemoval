package com.example.backgroundremoval.lib;

public class VisionSegmentPredictor extends VisionSegmentTFLPredictor{

    public VisionSegmentPredictor(SegmentOnDeviceModel segmentOnDeviceModel, VisionSegmentPredictorOptions options) {
        super(segmentOnDeviceModel, options);
    }
}
