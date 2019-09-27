package com.example.backgroundremoval.lib;

public interface PredictorStatusListener<T extends VisionPredictorBase>{
    void onPredictorReady(T predictor);
}
