package com.example.backgroundremoval.model;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Map;

public class VisionOrientation {
    private static final Map<Integer, Integer> ORIENTATIONS = new HashMap<>();

    static {
        ORIENTATIONS.put(Surface.ROTATION_0, 90);
        ORIENTATIONS.put(Surface.ROTATION_90, 0);
        ORIENTATIONS.put(Surface.ROTATION_180, 270);
        ORIENTATIONS.put(Surface.ROTATION_270, 180);
    }
    private static final String TAG = VisionOrientation.class.getSimpleName();

    /**
     * Using the device camera orientation and the device rotation, determine the rotation that should be applied to the image.
     *
     * @param activity
     * @param cameraId
     *
     * @return the rotation angle that should be applied to the image.
     */
    public static int getImageRotationFromCamera(Activity activity, String cameraId) {
        Log.d(TAG, "getImageRotationFromCamera: camId "+cameraId);
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "getImageRotationFromCamera: "+deviceRotation);
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        try {
            CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            Log.d(TAG, "getImageRotationFromCamera: sensor"+sensorOrientation);

            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Unable to access the camera " + cameraId + ":" + e.getMessage());
        }
        Log.d(TAG, "getImageRotationFromCamera: targetRotation : "+rotationCompensation);
        return rotationCompensation;
    }
}
