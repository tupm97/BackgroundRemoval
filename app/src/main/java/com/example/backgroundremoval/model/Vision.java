package com.example.backgroundremoval.model;

public class Vision {

    public static ImageSegmentationFeature ImageSegmentation = new ImageSegmentationFeature();

    public static class ImageSegmentationFeature extends FeatureBase<VisionSegmentPredictor, VisionSegmentPredictorOptions, SegmentOnDeviceModel, SegmentManagedModel> {

        private static final float DEFAULT_TARGET_CONFIDENCE_THRESHOLD = .3f;

        @Override
        protected VisionSegmentPredictorOptions getDefaultOptions() {
            return new VisionSegmentPredictorOptions.Builder()
                    .targetConfidenceThreshold(DEFAULT_TARGET_CONFIDENCE_THRESHOLD)
                    .build();
        }

        @Override
        public VisionSegmentPredictor getPredictor(SegmentOnDeviceModel onDeviceModel, VisionSegmentPredictorOptions options) {
            return new VisionSegmentPredictor(onDeviceModel, options);
        }

        @Override
        public void loadPredictor(final SegmentManagedModel segmentManagedModel, final VisionSegmentPredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final ModelManager modelManager = new ModelManager(segmentManagedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(OnDeviceModel onDeviceModel) {
                    SegmentOnDeviceModel segmentOnDeviceModel = new SegmentOnDeviceModel(
                            onDeviceModel.getModelPath(),
                            onDeviceModel.getModelId(),
                            onDeviceModel.getModelVersion(),
                            segmentManagedModel.getClassifications()
                    );
                    VisionSegmentPredictor predictor = new VisionSegmentPredictor(segmentOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }

        /**
         * Get the TFL Predictor.
         *
         * @deprecated Please use {@link #getPredictor}. This method will be removed in the next major version update.
         * @param onDeviceModel
         * @return
         */
        public VisionSegmentTFLPredictor getPredictorTFL(SegmentOnDeviceModel onDeviceModel) {
            return new VisionSegmentTFLPredictor(onDeviceModel, getDefaultOptions());
        }

        /**
         * Get the TFL Predictor.
         *
         * @deprecated Please use {@link #getPredictor}. This method will be removed in the next major version update.
         * @param onDeviceModel
         * @param options
         * @return
         */
        public VisionSegmentTFLPredictor getPredictorTFL(SegmentOnDeviceModel onDeviceModel, VisionSegmentPredictorOptions options) {
            return new VisionSegmentTFLPredictor(onDeviceModel, options);
        }

        /**
         * Get the TFL Predictor.
         *
         * @deprecated Please use {@link #loadPredictor}. This method will be removed in the next major version update.
         * @param segmentManagedModel
         * @param options
         * @param statusListener
         * @param useWifi
         * @return
         */
        public void loadPredictorTFL(final SegmentManagedModel segmentManagedModel, final VisionSegmentPredictorOptions options, final PredictorStatusListener statusListener, boolean useWifi) {
            final ModelManager modelManager = new ModelManager(segmentManagedModel);
            modelManager.loadModel(new ModelReadyListener() {
                @Override
                public void onModelReady(OnDeviceModel onDeviceModel) {
                    SegmentOnDeviceModel segmentOnDeviceModel = new SegmentOnDeviceModel(
                            onDeviceModel.getModelPath(),
                            onDeviceModel.getModelId(),
                            onDeviceModel.getModelVersion(),
                            segmentManagedModel.getClassifications()
                    );
                    VisionSegmentPredictor predictor = new VisionSegmentPredictor(segmentOnDeviceModel, options);
                    statusListener.onPredictorReady(predictor);
                }
            }, useWifi);
        }
    }
}
