package com.example.backgroundremoval.model;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class OnDeviceModel extends ManagedModel {


    private static final String TAG = OnDeviceModel.class.getSimpleName();

    private String modelPath;
    private int modelVersion;

    public OnDeviceModel(String modelPath, String modelId, int modelVersion) {
        super(modelId);
        this.modelPath = modelPath;
        this.modelVersion = modelVersion;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelPath() {
        return modelPath;
    }

    public int getModelVersion() {
        return modelVersion;
    }

    @Override
    public String toString() {
        return "Model " + modelId + "(version: " + modelVersion + ")";
    }


    /**
     * Checks if the custom model are equal.
     *
     * @param other The other FritzOnDeviceModel
     * @return true if equal, false otherwise
     * @hide
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof OnDeviceModel) {
            OnDeviceModel otherSettings = (OnDeviceModel) other;
            if (otherSettings.getModelId().equalsIgnoreCase(this.modelId) &&
                    otherSettings.getModelVersion() == this.modelVersion &&
                    otherSettings.getModelPath().equals(this.modelPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load the specified model file and return a MappedByteBuffer.
     *
     * @return a MappedByteBuffer of the loaded file
     * @throws IOException if the file can't be loaded.
     * @hide
     */
    public MappedByteBuffer readModelFile() throws IOException {

        Context context = Config.getAppContext();
         File file;
         FileInputStream inputStream= new FileInputStream(modelPath);
         FileChannel fileChannel=inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    /**
     * Delete the model file in storage.
     *
     * @return true/false if deleted
     * @hide
     */
    public boolean deleteModelFile() {
        return false;

    }
}
