package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class PrependTransformation extends AbstractTransformation {

    public PrependTransformation() {
        super(
                "prepend",
                "Prepend Text",
                List.of(
                        new ParameterDefinition(
                                "prefix",
                                "Prefix",
                                ParameterType.TEXT,
                                true,
                                "",
                                "Text to prepend"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if (input == null) {
            input = "";
        }
        String prefix = getStringParam(parameters, "prefix", "");
        return prefix + input;
    }
}
