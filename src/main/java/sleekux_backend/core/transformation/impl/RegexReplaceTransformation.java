package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexReplaceTransformation extends AbstractTransformation {

    public RegexReplaceTransformation() {
        super(
                "regex_replace",
                "Regex Replace",
                List.of(
                        new ParameterDefinition(
                                "pattern",
                                "Regex Pattern",
                                ParameterType.REGEX,
                                true,
                                "",
                                "e.g. \\s+ matches runs of whitespace"
                        ),
                        new ParameterDefinition(
                                "replacement",
                                "Replacement",
                                ParameterType.TEXT,
                                false,
                                "",
                                "Text for each match, $1 inserts group 1, empty deletes"
                        ),
                        new ParameterDefinition(
                                "caseInsensitive",
                                "Ignore Case",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Match regardless of upper/lower case"
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

        String replacement = getStringParam(parameters, "replacement", "");
        boolean caseInsensitive = getBooleanParam(parameters, "caseInsensitive", false);

        int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
        Pattern pattern = Pattern.compile(patternStr, flags);

        try {
            return pattern.matcher(input).replaceAll(replacement);
        } catch (IndexOutOfBoundsException e) {
            // Thrown for a group reference like $5 that does not exist in the pattern
            throw new IllegalArgumentException("Invalid replacement: " + e.getMessage());
        }
    }
}
