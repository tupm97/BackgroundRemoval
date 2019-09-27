package com.example.backgroundremoval.lib;

class SegmentOnDeviceModel  extends OnDeviceModel {
    private MaskType[] classifications;

    public static SegmentOnDeviceModel mergeFromManagedModel(OnDeviceModel onDeviceModel, SegmentManagedModel managedModel) {
        return new SegmentOnDeviceModel(
                onDeviceModel.getModelPath(),
                onDeviceModel.getModelId(),
                onDeviceModel.getModelVersion(),
                managedModel.getClassifications());
    }

    public SegmentOnDeviceModel(String modelPath, String modelId, int modelVersion, MaskType[] classifications) {
        super(modelPath, modelId, modelVersion);
        this.classifications = classifications;
    }

    public MaskType[] getClassifications() {
        return classifications;
    }
}
