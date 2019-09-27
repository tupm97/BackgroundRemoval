package com.example.backgroundremoval.lib;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class TFLiteInterpreter  extends MyInterpreter<Interpreter>{
    private static final String TAG = TFLiteInterpreter.class.getSimpleName();

    private static Logger logger = Logger.getLogger(
            TFLiteInterpreter.class.getSimpleName());

    private Interpreter interpreter;
    private Interpreter.Options interpreterOptions;

    public TFLiteInterpreter(OnDeviceModel currentOnDeviceModel) {
        this(currentOnDeviceModel, new Interpreter.Options());
    }

    public TFLiteInterpreter(OnDeviceModel currentOnDeviceModel, Interpreter.Options interpreterOptions) {
        super(currentOnDeviceModel);
        try {
            this.interpreter = new Interpreter(onDeviceModel.readModelFile(), interpreterOptions);
            this.interpreterOptions = interpreterOptions;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the number of input tensors.
     * @return the count
     */
    public int getInputTensorCount() {
        return interpreter.getInputTensorCount();
    }

    /**
     * Get the number of output tensors.
     * @return the count
     */
    public int getOutputTensorCount() {
        return interpreter.getOutputTensorCount();
    }

    /**
     * Get the input tensor at the specific index.
     * @return the tensor
     */
    public Tensor getInputTensor(int inputIndex) {
        return interpreter.getInputTensor(inputIndex);
    }

    /**
     * Get the output tensor at the specific index.
     * @return the tensor
     */
    public Tensor getOutputTensor(int outputIndex) {
        return interpreter.getOutputTensor(outputIndex);
    }

    @Override
    public Interpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public void checkRefreshInterpreter(OnDeviceModel newOnDeviceModel) {

        if (!shouldRefreshInterpreter(onDeviceModel, newOnDeviceModel)) {
            return;
        }
        try {
            Interpreter newInterpreter = new Interpreter(newOnDeviceModel.readModelFile(), this.interpreterOptions);
            OnDeviceModel previousOnDeviceModel = onDeviceModel;
            onDeviceModel = newOnDeviceModel;
            interpreter = newInterpreter;
            previousOnDeviceModel.deleteModelFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run model inference on the input and output methods.
     * <p>
     * The interpreter will record metrics on model execution.
     */
    public void run(Object input, Object output) {
//        modelDownloadManager.checkForNewActiveVersion();
        this.interpreter.run(input, output);
        trackInferenceTime();
    }

    /**
     * Runs model inference for multiple inputs / outputs.
     * <p>
     * The interpreter will record metrics on model execution.
     *
     * @param inputs
     * @param outputs
     */
    public void runForMultipleInputsOutputs(Object[] inputs, Map<Integer, Object> outputs) {
        //modelDownloadManager.checkForNewActiveVersion();
        this.interpreter.runForMultipleInputsOutputs(inputs, outputs);
        trackInferenceTime();
    }

    private void trackInferenceTime() {
        // Only track the run methods
        Long elapsed = getLastNativeInferenceDurationNanoseconds();
        if (elapsed == null) {
            return;
        }
//        EventTracker.getInstance().trackPrediction(onDeviceModel, elapsed);
    }

    /**
     * Resizes idx-th input of the native model to the given dims.
     *
     * @param idx  index
     * @param dims dimensions
     */
    public void resizeInput(int idx, int[] dims) {
        this.interpreter.resizeInput(idx, dims);
    }

    /**
     * Gets index of an input given the op name of the input.
     *
     * @param opName operation name
     */
    public int getInputIndex(String opName) {
        return this.interpreter.getInputIndex(opName);
    }

    /**
     * Gets index of an output given the op name of the output.
     *
     * @param opName operation name
     */
    public int getOutputIndex(String opName) {
        return this.interpreter.getOutputIndex(opName);
    }

    /**
     * Turns on/off Android NNAPI for hardware acceleration when it is available.
     *
     * @param useNNAPI true/false should use NNAPI
     */
    public void setUseNNAPI(boolean useNNAPI) {
        this.interpreter.setUseNNAPI(useNNAPI);
    }

    /**
     * Release resources associated with the {@code Interpreter}.
     */
    public void close() {
        this.interpreter.close();
    }

    public void setNumThreads(int numThreads) {
        this.interpreter.setNumThreads(numThreads);
    }

    /**
     * Get the inference timing in nanoseconds.
     */
    public Long getLastNativeInferenceDurationNanoseconds() {
        return this.interpreter.getLastNativeInferenceDurationNanoseconds();
    }
}
