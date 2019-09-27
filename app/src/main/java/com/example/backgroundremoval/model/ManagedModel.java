package com.example.backgroundremoval.model;

public class ManagedModel {

    public static final String MODEL_ID_KEY = "model_id";

    protected String modelId;

    public ManagedModel(String modelId) {
        this.modelId = modelId;
    }
    public String getModelId() {
        return modelId;
    }

}
