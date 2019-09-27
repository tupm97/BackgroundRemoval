package com.example.backgroundremoval.model;

public interface PredictorStatusListener<T extends VisionPredictorBase>{
    void onPredictorReady(T predictor);
}
