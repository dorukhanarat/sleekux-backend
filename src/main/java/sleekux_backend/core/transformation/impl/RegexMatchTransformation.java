package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchTransformation extends AbstractTransformation {


    public RegexMatchTransformation() {
        super(
                "regex_match",
                "Regex Match",
                List.of(
                        new ParameterDefinition(
                                "pattern",
                                "Regex Pattern",
                                ParameterType.REGEX,
                                true,
                                "",
                                "e.g. \\d+ finds every number"
                        ),
                        new ParameterDefinition(
                                "matchAll",
                                "Match All",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Turn off to return only the first match"
                        ),
                        new ParameterDefinition(
                                "delimiter",
                                "Delimiter",
                                ParameterType.TEXT,
                                false,
                                ", ",
                                "Text placed between matches, default is a comma and a space"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        String patternStr = getStringParam(parameters, "pattern", "");
        if(patternStr.isEmpty()) {
            return input;
        }

        boolean matchAll = getBooleanParam(parameters, "matchAll", true);
        String delimiter = getStringParam(parameters, "delimiter", ", ");

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);

        if(!matcher.find()) {
            return "";
        }

        if(!matchAll) {
            return matcher.group();
        }

        List<String> matches = new ArrayList<>();
        do {
            matches.add(matcher.group());
        } while (matcher.find());

        return String.join(delimiter, matches);
    }
}
