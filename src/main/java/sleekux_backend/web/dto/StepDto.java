package sleekux_backend.web.dto;

import java.util.Map;

public class StepDto {

    private String transformationKey;
    private Map<String, Object> parameters;

    public StepDto() {
    }

    public StepDto(String transformationKey, Map<String, Object> parameters) {
        this.transformationKey = transformationKey;
        this.parameters = parameters;
    }

    public String getTransformationKey() {
        return transformationKey;
    }

    public void setTransformationKey(String transformationKey) {
        this.transformationKey = transformationKey;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
