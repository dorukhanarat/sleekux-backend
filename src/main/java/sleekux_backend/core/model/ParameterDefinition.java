package sleekux_backend.core.model;

import java.util.Objects;

public final class ParameterDefinition {
    private final String name;
    private final String label;
    private final ParameterType type;
    private final boolean required;
    private final Object defaultValue;
    private final String placeholder;

    public ParameterDefinition(String name, String label, ParameterType type, boolean required, Object defaultValue, String placeholder) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.placeholder = placeholder;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public ParameterType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ParameterDefinition that = (ParameterDefinition) o;
        return required == that.required && Objects.equals(name, that.name) && Objects.equals(label, that.label) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, label, type, required);
    }

    @Override
    public String toString() {
        return "ParameterDefinition{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", required=" + required +
                '}';
    }
}
