package com.example.backgroundremoval.model;

public enum CameraRotation {

    DEGREES_0(0),
    DEGREES_90(90),
    DEGREES_180(180),
    DEGREES_270(270);

    private int degrees;

    CameraRotation(int degrees) {
        this.degrees = degrees;
    }

    public int getDegrees() {
        return degrees;
    }

    public static CameraRotation getRotation(int degrees) {
        for (CameraRotation cameraRotation : CameraRotation.values()) {
            if (cameraRotation.getDegrees() == degrees) {
                return cameraRotation;
            }
        }

        return DEGREES_0;
    }
}
