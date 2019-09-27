package com.example.backgroundremoval.lib;

public class ModelManager {

    private static final String TAG = ModelManager.class.getSimpleName();

    protected String modelId;
    protected OnDeviceModel currentOnDeviceModel;

    public ModelManager(ManagedModel managedModel) {
        this.modelId = managedModel.getModelId();
    }

    /**
     * Download the latest model version.
     *
     * @param statusListener
     */
    public void loadModel(final ModelReadyListener statusListener) {
        loadModel(statusListener, false);
    }

    /**
     * Download the latest model version.
     *
     * @param statusListener - a callback when the download finishes or when an onDevice model exists.
     * @param useWifi - if the download should only happen with wifi.
     */
    public void loadModel(final ModelReadyListener statusListener, boolean useWifi) {
        if (currentOnDeviceModel != null) {
            statusListener.onModelReady(currentOnDeviceModel);
            return;
        }
    }
}
