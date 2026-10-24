package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SplitJoinTransformation extends AbstractTransformation {

    public SplitJoinTransformation() {
        super(
                "split_join",
                "Split & Join",
                List.of(
                        new ParameterDefinition(
                                "splitBy",
                                "Split By",
                                ParameterType.TEXT,
                                false,
                                ",",
                                "Separator in the input, e.g. , or ; or |"
                        ),
                        new ParameterDefinition(
                                "joinWith",
                                "Join With",
                                ParameterType.TEXT,
                                false,
                                "\n",
                                "Separator in the result, default is a new line"
                        ),
                        new ParameterDefinition(
                                "trimItems",
                                "Trim Items",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Remove whitespace around each item"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String splitBy = getStringParam(parameters, "splitBy", ",");
        if(splitBy.isEmpty()) {
            return input;
        }

        String joinWith = getStringParam(parameters, "joinWith", "\n");
        boolean trimItems = getBooleanParam(parameters, "trimItems", true);

        // splitBy is matched literally, not as a regex
        return Arrays.stream(input.split(Pattern.quote(splitBy), -1))
                .map(item -> trimItems ? item.strip() : item)
                .collect(Collectors.joining(joinWith));
    }
}
