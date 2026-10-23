package sleekux_backend.core.transformation;

import sleekux_backend.core.model.ParameterDefinition;

import java.util.List;
import java.util.Map;

public abstract class AbstractTransformation implements Transformation {
    private final String key;
    private final String displayName;
    private final List<ParameterDefinition> parameters;

    protected AbstractTransformation(String key, String displayName, List<ParameterDefinition> parameters) {
        this.key = key;
        this.displayName = displayName;
        this.parameters = parameters;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    protected String getStringParam(Map<String, Object> params, String name, String defaultValue) {
        if(params == null || !params.containsKey(name) || params.get(name) == null) {
            return defaultValue;
        }
        return String.valueOf(params.get(name));
    }

    protected int getIntParam(Map<String, Object> params, String name, int defaultValue) {
        if(params == null || !params.containsKey(name) || params.get(name) == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(params.get(name)).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected boolean getBooleanParam(Map<String, Object> params, String name, boolean defaultValue) {
        if(params == null || !params.containsKey(name) || params.get(name) == null) {
            return defaultValue;
        }
        String value = String.valueOf(params.get(name)).trim();
        if(value.equalsIgnoreCase("true")) {
            return true;
        }
        if(value.equalsIgnoreCase("false")) {
            return false;
        }
        return defaultValue;
    }

}
