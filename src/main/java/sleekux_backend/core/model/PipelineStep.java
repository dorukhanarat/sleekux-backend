package sleekux_backend.core.model;

import java.util.Collections;
import java.util.Map;

public class PipelineStep {

    private final String transformationKey;
    private final Map<String, Object> parameters;

    public PipelineStep(String transformationKey, Map<String, Object> parameters) {
        if(transformationKey == null || transformationKey.isBlank()) {
            throw new IllegalArgumentException("transformationKey cannot be null or blank");
        }
        this.transformationKey = transformationKey;
        this.parameters = parameters != null ? Collections.unmodifiableMap(parameters) : Collections.emptyMap();
    }

    public String getTransformationKey() {
        return transformationKey;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "PipelineStep{" +
                "transformationKey='" + transformationKey + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
