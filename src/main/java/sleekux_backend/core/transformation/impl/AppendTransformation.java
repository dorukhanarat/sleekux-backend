package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class AppendTransformation extends AbstractTransformation {

    public AppendTransformation() {
        super(
                "append",
                "Append Text",
                List.of(
                        new ParameterDefinition(
                                "suffix",
                                "Suffix",
                                ParameterType.TEXT,
                                true,
                                "",
                                "Text added to the end, e.g. .txt"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            input = "";
        }
        String suffix = getStringParam(parameters, "suffix", "");
        return input + suffix;
    }
}
