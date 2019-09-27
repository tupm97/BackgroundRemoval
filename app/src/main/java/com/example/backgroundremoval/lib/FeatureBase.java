package com.example.backgroundremoval.lib;

public abstract class FeatureBase<Predictor extends VisionPredictorBase, Options extends VisionPredictorOptions, MyOnDeviceModel extends OnDeviceModel, MyManagedModel extends ManagedModel> {

    public Predictor getPredictor(MyOnDeviceModel onDeviceModel) {
        return getPredictor(onDeviceModel, getDefaultOptions());
    }

    public void loadPredictor(final MyManagedModel managedModel, final PredictorStatusListener statusListener) {
        loadPredictor(managedModel, getDefaultOptions(), statusListener, false);
    }

    public void loadPredictor(final MyManagedModel managedModel, final PredictorStatusListener statusListener, boolean useWifi) {
        loadPredictor(managedModel, getDefaultOptions(), statusListener, useWifi);
    }

    public void loadPredictor(final MyManagedModel managedModel, final Options options, final PredictorStatusListener statusListener) {
        loadPredictor(managedModel, options, statusListener, false);
    }

    public abstract Predictor getPredictor(MyOnDeviceModel onDeviceModel, Options options);

    public abstract void loadPredictor(final MyManagedModel managedModel, final Options options, final PredictorStatusListener statusListener, boolean useWifi);

    protected abstract Options getDefaultOptions();
}
