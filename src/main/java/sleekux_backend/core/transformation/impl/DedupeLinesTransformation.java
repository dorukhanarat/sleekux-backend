package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DedupeLinesTransformation extends AbstractTransformation {

    public DedupeLinesTransformation() {
        super(
                "dedupe_lines",
                "Remove Duplicate Lines",
                List.of(
                        new ParameterDefinition(
                                "caseSensitive",
                                "Case Sensitive",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Treat \"Apple\" and \"apple\" as different lines"
                        ),
                        new ParameterDefinition(
                                "trimLines",
                                "Ignore Surrounding Whitespace",
                                ParameterType.BOOLEAN,
                                false,
                                false,
                                "Compare lines without leading/trailing whitespace"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        boolean caseSensitive = getBooleanParam(parameters, "caseSensitive", true);
        boolean trimLines = getBooleanParam(parameters, "trimLines", false);

        // Keeps the first occurrence of each line, in its original form
        Set<String> seen = new HashSet<>();
        List<String> result = new ArrayList<>();
        for(String line : input.split("\\R", -1)) {
            String key = trimLines ? line.strip() : line;
            if(!caseSensitive) {
                key = key.toLowerCase(Locale.ROOT);
            }
            if(seen.add(key)) {
                result.add(line);
            }
        }
        return String.join("\n", result);
    }
}
