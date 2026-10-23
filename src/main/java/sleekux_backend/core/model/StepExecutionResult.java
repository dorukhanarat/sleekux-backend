package sleekux_backend.core.model;

import java.util.Map;
import java.util.Objects;

public class StepExecutionResult {
    private final int stepIndex;
    private final String transformationKey;
    private final String transformationName;
    private final Map<String, Object> parameters;
    private final String inputSnapshot;
    private final String outputSnapshot;
    private final long executionTime;

    public StepExecutionResult(int stepIndex,
                               String transformationKey,
                               String transformationName,
                               Map<String, Object> parameters,
                               String inputSnapshot,
                               String outputSnapshot,
                               long executionTime) {
        this.stepIndex = stepIndex;
        this.transformationKey = transformationKey;
        this.transformationName = transformationName;
        this.parameters = parameters;
        this.inputSnapshot = inputSnapshot;
        this.outputSnapshot = outputSnapshot;
        this.executionTime = executionTime;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getTransformationKey() {
        return transformationKey;
    }

    public String getTransformationName() {
        return transformationName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getInputSnapshot() {
        return inputSnapshot;
    }

    public String getOutputSnapshot() {
        return outputSnapshot;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StepExecutionResult that = (StepExecutionResult) o;
        return stepIndex == that.stepIndex && Objects.equals(transformationKey, that.transformationKey) && Objects.equals(transformationName, that.transformationName) && Objects.equals(parameters, that.parameters) && Objects.equals(inputSnapshot, that.inputSnapshot) && Objects.equals(outputSnapshot, that.outputSnapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepIndex, transformationKey, transformationName, parameters, inputSnapshot, outputSnapshot);
    }

    @Override
    public String toString() {
        return "StepExecutionResult{" +
                "stepIndex=" + stepIndex +
                ", transformationKey='" + transformationKey + '\'' +
                ", transformationName='" + transformationName + '\'' +
                ", parameters=" + parameters +
                ", inputSnapshot='" + inputSnapshot + '\'' +
                ", outputSnapshot='" + outputSnapshot + '\'' +
                ", executionTime=" + executionTime +
                '}';
    }
}
