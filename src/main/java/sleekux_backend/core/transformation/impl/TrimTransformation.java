package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrimTransformation extends AbstractTransformation {

    public TrimTransformation() {
        super(
                "trim",
                "Trim Whitespace",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Trim Mode",
                                ParameterType.TEXT,
                                false,
                                "both",
                                "start, end or both"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        String mode = getStringParam(parameters, "mode", "both");

        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "start" -> input.stripLeading();
            case "end" -> input.stripTrailing();
            default -> input.strip();
        };
    }
}
