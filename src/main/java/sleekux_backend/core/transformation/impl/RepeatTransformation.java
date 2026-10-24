package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RepeatTransformation extends AbstractTransformation {

    static final int MAX_COUNT = 1000;

    public RepeatTransformation() {
        super(
                "repeat",
                "Repeat Text",
                List.of(
                        new ParameterDefinition(
                                "count",
                                "Count",
                                ParameterType.NUMBER,
                                false,
                                2,
                                "Number of copies, up to 1000"
                        ),
                        new ParameterDefinition(
                                "separator",
                                "Separator",
                                ParameterType.TEXT,
                                false,
                                "",
                                "Text placed between copies, e.g. a comma"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        int count = Math.max(0, Math.min(getIntParam(parameters, "count", 2), MAX_COUNT));
        String separator = getStringParam(parameters, "separator", "");

        return String.join(separator, Collections.nCopies(count, input));
    }
}
