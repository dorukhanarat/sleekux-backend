package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class SubstringTransformation extends AbstractTransformation {

    public SubstringTransformation() {
        super(
                "substring",
                "Substring",
                List.of(
                        new ParameterDefinition(
                                "startIndex",
                                "Start Index",
                                ParameterType.NUMBER,
                                true,
                                0,
                                "Index of the first character to keep, 0 is the start"
                        ),
                        new ParameterDefinition(
                                "endIndex",
                                "End Index",
                                ParameterType.NUMBER,
                                false,
                                -1,
                                "Index to stop before, -1 keeps everything to the end"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        int length = input.length();
        int startIndex = getIntParam(parameters, "startIndex", 0);
        int endIndex = getIntParam(parameters, "endIndex", -1);

        if(startIndex < 0) {
            startIndex = 0;
        }

        if(startIndex > length) {
            return "";
        }

        if(endIndex < 0 || endIndex > length) {
            endIndex = length;
        }

        if(startIndex >= endIndex) {
            return "";
        }

        return input.substring(startIndex, endIndex);
    }
}
