package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NormalizeWhitespaceTransformation extends AbstractTransformation {

    // (?U) makes \s match Unicode whitespace too, consistent with String.strip()
    private static final Pattern WHITESPACE = Pattern.compile("(?U)\\s+");

    public NormalizeWhitespaceTransformation() {
        super(
                "normalize_whitespace",
                "Normalize Whitespace",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "collapse",
                                "collapse (single spaces), all (remove) or blank_lines"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        String mode = getStringParam(parameters, "mode", "collapse");

        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "all" -> WHITESPACE.matcher(input).replaceAll("");
            case "blank_lines" -> Arrays.stream(input.split("\\R", -1))
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.joining("\n"));
            default -> WHITESPACE.matcher(input).replaceAll(" ").strip();
        };
    }
}
