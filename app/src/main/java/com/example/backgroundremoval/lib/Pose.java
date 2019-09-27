package com.example.backgroundremoval.lib;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Pair;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

public class Pose {

    private Keypoint[] keypoints;
    private float poseScore;
    private float keypointThreshold;

    public Pose(Keypoint[] keypoints, float poseScore, float keypointThreshold) {
        this.keypoints = keypoints;
        this.poseScore = poseScore;
        this.keypointThreshold = keypointThreshold;
    }

    /**
     * Get all keypoints for this Pose.
     *
     * @return an array of keypoints.
     */
    public Keypoint[] getKeypoints() {
        return keypoints;
    }

    /**
     * Get the score of the Pose
     *
     * @return a float score from 0-1
     */
    public float getScore() {
        return poseScore;
    }

    /**
     * Get the keypoint threshold.
     *
     * @return the threshold.
     */
    public float getKeypointThreshold() {
        return keypointThreshold;
    }

    /**
     * Calculates relative keypoint position to target coordinates.
     *
     * The keypoint positions are all relative to the modelSize initially. To convert them to a
     * target size, use this method.
     *
     * @param modelSize   - the original model size that all keypoint positions are currently based on.
     * @param scaleToSize - the size to scale to
     * @param offsetX     - how much to offset the X position (if the model ran on a subset of the image)
     * @param offsetY     - how much to offset the Y position (if the model ran on a subset of the image)
     */
    public void calculateScaledPose(Size modelSize, Size scaleToSize, int offsetX, int offsetY) {
        for (Keypoint keypoint : keypoints) {
            PointF positionForModelSize = keypoint.getPosition();

            float widthScaled = (float) scaleToSize.getWidth() / modelSize.getWidth();
            float heightScaled = (float) scaleToSize.getHeight() / modelSize.getHeight();

            float xScaled = positionForModelSize.x * widthScaled + offsetX;
            float yScaled = positionForModelSize.y * heightScaled + offsetY;

            keypoint.setPosition(new PointF(xScaled, yScaled));
        }
    }

    /**
     * Get a list of the connected keypoints if the keypoint confidence scores are higher than a given threshold.
     * @return a list of connected keypoints to draw a line between.
     */
    public List<Pair<Keypoint, Keypoint>> getConnectedKeypoints() {
        List<Pair<Keypoint, Keypoint>> connectedKeypoints = new ArrayList<>();
        for (Pair<Integer, Integer> connectedParts : PoseTypes.CONNECTED_PART_INDICIES) {
            Keypoint leftKeypoint = keypoints[connectedParts.first];
            Keypoint rightKeypoint = keypoints[connectedParts.second];

            if (leftKeypoint.getScore() >= keypointThreshold && rightKeypoint.getScore() >= keypointThreshold) {
                connectedKeypoints.add(new Pair<Keypoint, Keypoint>(leftKeypoint, rightKeypoint));
            }
        }

        return connectedKeypoints;
    }

    /**
     * Draw the Pose on a canvas.
     *
     * @param canvas - the canvas to draw on.
     */
    public void draw(Canvas canvas) {
        for (Keypoint keypoint : keypoints) {
            canvas.drawCircle(keypoint.getPosition().x, keypoint.getPosition().y, 5, DrawingUtils.DEFAULT_PAINT);
        }

        for (Pair<Keypoint, Keypoint> connectedKeypoints : getConnectedKeypoints()) {
            Keypoint left = connectedKeypoints.first;
            Keypoint right = connectedKeypoints.second;
            canvas.drawLine(left.getPosition().x, left.getPosition().y, right.getPosition().x, right.getPosition().y, DrawingUtils.DEFAULT_PAINT);
        }
    }
}
