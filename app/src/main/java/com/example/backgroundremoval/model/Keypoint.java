package com.example.backgroundremoval.model;

import android.graphics.PointF;

public class Keypoint {

    private int id;
    private PointF position;
    private float score;

    public Keypoint(int id, PointF position, float score) {
        this.id = id;
        this.position = position;
        this.score = score;
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public float getScore() {
        return score;
    }

    /**
     * This will return one of the 17 identified body parts:
     * <p>
     * - nose
     * - leftEye
     * - rightEye
     * - leftEar
     * - rightEar
     * - leftShoulder
     * - rightShoulder
     * - leftElbow
     * - rightElbow
     * - leftWrist
     * - rightWrist
     * - leftHip
     * - rightHip
     * - leftKnee
     * - rightKnee
     * - leftAnkle
     * - rightAnkle
     *
     * @return the name of the body part.
     */
    public String getPartName() {
        return PoseTypes.PART_NAMES[id];
    }

    public int getId() {
        return id;
    }

    public float calculateSquaredDistanceFromCoordinates(PointF coordinates) {
        float dx = position.x - coordinates.x;
        float dy = position.y - coordinates.y;
        return dx * dx + dy * dy;
    }
}
