package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class ReplaceTransformation extends AbstractTransformation {

    public ReplaceTransformation() {
        super(
                "replace",
                "Replace Text",
                List.of(
                        new ParameterDefinition(
                                "oldValue",
                                "Target String",
                                ParameterType.TEXT,
                                true,
                                "",
                                "Exact text to find, e.g. world"
                        ),
                        new ParameterDefinition(
                                "newValue",
                                "Replacement String",
                                ParameterType.TEXT,
                                false,
                                "",
                                "Text to put in its place, leave empty to delete"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String oldValue = getStringParam(parameters, "oldValue", "");
        String newValue = getStringParam(parameters, "newValue", "");

        if(oldValue.isEmpty()) {
            return input;
        }

        return input.replace(oldValue, newValue);
    }
}
