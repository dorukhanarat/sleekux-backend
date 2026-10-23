package sleekux_backend.core.transformation;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

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
                                "Text to replace" ),
                        new ParameterDefinition(
                                "newValue",
                                "Replacement String",
                                ParameterType.TEXT,
                                false,
                                "",
                                "")
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
