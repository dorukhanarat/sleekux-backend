package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterLinesTransformation extends AbstractTransformation {

    public FilterLinesTransformation() {
        super(
                "filter_lines",
                "Filter Lines",
                List.of(
                        new ParameterDefinition(
                                "pattern",
                                "Regex Pattern",
                                ParameterType.REGEX,
                                true,
                                "",
                                "e.g. ^ERROR keeps lines starting with ERROR"
                        ),
                        new ParameterDefinition(
                                "invert",
                                "Invert",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Remove matching lines instead of keeping them"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String patternStr = getStringParam(parameters, "pattern", "");
        if(patternStr.isEmpty()) {
            return input;
        }

        boolean invert = getBooleanParam(parameters, "invert", false);
        Pattern pattern = Pattern.compile(patternStr);

        return Arrays.stream(input.split("\\R", -1))
                .filter(line -> pattern.matcher(line).find() != invert)
                .collect(Collectors.joining("\n"));
    }
}
