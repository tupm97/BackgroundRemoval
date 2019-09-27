package com.example.backgroundremoval.model;

class SegmentManagedModel extends ManagedModel {
    private MaskType[] classifications;

    public SegmentManagedModel(String modelId, MaskType[] classifications) {
        super(modelId);
        this.classifications = classifications;
    }

    public MaskType[] getClassifications() {
        return classifications;
    }
}
