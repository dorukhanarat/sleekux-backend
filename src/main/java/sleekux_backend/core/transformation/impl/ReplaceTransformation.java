package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        ),
                        new ParameterDefinition(
                                "caseInsensitive",
                                "Ignore Case",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Also match World and WORLD when searching for world"
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
        boolean caseInsensitive = getBooleanParam(parameters, "caseInsensitive", false);

        if(oldValue.isEmpty()) {
            return input;
        }

        if(!caseInsensitive) {
            return input.replace(oldValue, newValue);
        }

        // Both values are quoted, so regex characters in them are still treated literally
        return Pattern.compile(Pattern.quote(oldValue), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(input)
                .replaceAll(Matcher.quoteReplacement(newValue));
    }
}
