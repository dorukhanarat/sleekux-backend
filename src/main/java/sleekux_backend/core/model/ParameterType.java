package sleekux_backend.core.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ParameterType {
    TEXT("text"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    REGEX("regex");

    private final String value;

    ParameterType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
