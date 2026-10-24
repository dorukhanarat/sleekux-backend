package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumberLinesTransformation extends AbstractTransformation {

    public NumberLinesTransformation() {
        super(
                "number_lines",
                "Number Lines",
                List.of(
                        new ParameterDefinition(
                                "start",
                                "Start Number",
                                ParameterType.NUMBER,
                                false,
                                1,
                                "Number of the first line"
                        ),
                        new ParameterDefinition(
                                "separator",
                                "Separator",
                                ParameterType.TEXT,
                                false,
                                ". ",
                                "Text after each number, e.g. ) or :"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        int start = getIntParam(parameters, "start", 1);
        String separator = getStringParam(parameters, "separator", ". ");

        String[] lines = input.split("\\R", -1);
        List<String> numbered = new ArrayList<>(lines.length);
        for(int i = 0; i < lines.length; i++) {
            numbered.add((start + i) + separator + lines[i]);
        }
        return String.join("\n", numbered);
    }
}
