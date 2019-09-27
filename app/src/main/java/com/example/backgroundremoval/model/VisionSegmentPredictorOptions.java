package com.example.backgroundremoval.model;

import java.util.List;

public class VisionSegmentPredictorOptions extends VisionPredictorOptions{

    private int numThreads;
    private List<MaskType> maskTypes;
    private float confidenceThreshold;

    private VisionSegmentPredictorOptions(Builder options) {
        super(options.cropAndScaleOption);
        this.confidenceThreshold = options.confidenceThreshold;
        this.maskTypes = options.maskTypes;
        this.numThreads = options.numThreads;
    }

    /**
     * Get the confidence threshold.
     * <p>
     * Filters out objects less than the confidence threshold.
     *
     * @return threshold between 0-1
     */
    public float getTargetConfidenceThreshold() {
        return confidenceThreshold;
    }

    /**
     * Gets the topN objects (sorted by confidence score).
     *
     * @return an integer specifying the N.
     */
    public List<MaskType> getTargetSegments() {
        return maskTypes;
    }

    /**
     * Get the number of threads to use for the TFL interpreter
     *
     * @return the number of threads.
     */
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Builder for FritzVisionObjectPredictorOptions
     */
    public static class Builder {
        private float confidenceThreshold = 0;
        private List<MaskType> maskTypes = null;
        private int numThreads = Runtime.getRuntime().availableProcessors();

        // Scale to fit makes more sense to use for image segmentation.
        private VisionCropAndScale cropAndScaleOption = VisionCropAndScale.SCALE_TO_FIT;

        /**
         * Set the crop and scale option.
         *
         * @param cropAndScaleOption
         * @return the builder.
         */
        public Builder cropAndScaleOption(VisionCropAndScale cropAndScaleOption) {
            this.cropAndScaleOption = cropAndScaleOption;
            return this;
        }

        public Builder targetSegmentClasses(List<MaskType> maskTypes) {
            this.maskTypes = maskTypes;
            return this;
        }

        /**
         * Sets the confidence threshold for the predictor.
         * <p>
         * The predictor will automatically filter out objects that have a
         * confidence threshold lower than this specified amount.
         *
         * @param confidenceThreshold the threshold for predictions.
         * @return
         */
        public Builder targetConfidenceThreshold(float confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
            return this;
        }

        /**
         * Set the number of threads to use in the underlying TFL interpreter.
         *
         * @param numThreads
         * @return
         */
        public Builder numThreads(int numThreads) {
            this.numThreads = numThreads;
            return this;
        }

        /**
         * Build the options.
         *
         * @return the created options for the predictor.
         */
        public VisionSegmentPredictorOptions build() {
            return new VisionSegmentPredictorOptions(this);
        }
    }
}
