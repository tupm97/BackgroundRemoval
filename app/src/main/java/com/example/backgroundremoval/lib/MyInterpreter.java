package com.example.backgroundremoval.lib;

public abstract class MyInterpreter<T> {
    private static final String TAG = MyInterpreter.class.getSimpleName();

    protected OnDeviceModel onDeviceModel;

    public MyInterpreter(OnDeviceModel currentOnDeviceModel) {
        this.onDeviceModel = currentOnDeviceModel;
    }

    public abstract T getInterpreter();

    public abstract void checkRefreshInterpreter(OnDeviceModel newOnDeviceModel);

    protected boolean shouldRefreshInterpreter(OnDeviceModel currentOnDeviceModel, OnDeviceModel activeOnDeviceModel) {

        return activeOnDeviceModel.getModelVersion() != currentOnDeviceModel.getModelVersion();
    }

    public OnDeviceModel getOnDeviceModel() {
        return onDeviceModel;
    }

}
