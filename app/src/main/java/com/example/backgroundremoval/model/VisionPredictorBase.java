package com.example.backgroundremoval.model;

public abstract class VisionPredictorBase<T> {

    public abstract T predict(VisionImage visionImage);
}
