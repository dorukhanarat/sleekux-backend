package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeLinesTransformation extends AbstractTransformation {

    public TakeLinesTransformation() {
        super(
                "take_lines",
                "Take Lines",
                List.of(
                        new ParameterDefinition(
                                "count",
                                "Number of Lines",
                                ParameterType.NUMBER,
                                true,
                                10,
                                "How many lines to keep, e.g. 10"
                        ),
                        new ParameterDefinition(
                                "from",
                                "From",
                                ParameterType.TEXT,
                                false,
                                "start",
                                "start keeps the first lines, end keeps the last lines"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        int count = getIntParam(parameters, "count", 10);
        String from = getStringParam(parameters, "from", "start");

        if(count <= 0) {
            return "";
        }

        List<String> lines = splitLines(input);
        int size = lines.size();
        List<String> kept = from.toLowerCase(Locale.ROOT).equals("end")
                ? lines.subList(Math.max(0, size - count), size)
                : lines.subList(0, Math.min(count, size));

        return String.join("\n", kept);
    }
}
