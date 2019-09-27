package com.example.backgroundremoval.lib;

public abstract class VisionPredictorBase<T> {

    public abstract T predict(VisionImage visionImage);
}
