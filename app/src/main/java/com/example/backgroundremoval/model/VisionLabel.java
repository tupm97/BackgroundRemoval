package com.example.backgroundremoval.model;

public class VisionLabel {

    private String text;
    private float confidence;

    public VisionLabel(String text, float confidence) {
        this.text = text;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public float getConfidence() {
        return confidence;
    }
}
