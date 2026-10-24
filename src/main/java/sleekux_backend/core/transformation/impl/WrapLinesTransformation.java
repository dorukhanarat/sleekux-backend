package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WrapLinesTransformation extends AbstractTransformation {

    public WrapLinesTransformation() {
        super(
                "wrap_lines",
                "Wrap Each Line",
                List.of(
                        new ParameterDefinition(
                                "prefix",
                                "Prefix",
                                ParameterType.TEXT,
                                false,
                                "",
                                "Added before each line, e.g. '"
                        ),
                        new ParameterDefinition(
                                "suffix",
                                "Suffix",
                                ParameterType.TEXT,
                                false,
                                "",
                                "Added after each line, e.g. ',"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        String prefix = getStringParam(parameters, "prefix", "");
        String suffix = getStringParam(parameters, "suffix", "");

        return Arrays.stream(input.split("\\R", -1))
                .map(line -> prefix + line + suffix)
                .collect(Collectors.joining("\n"));
    }
}
