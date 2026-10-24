package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReverseTransformation extends AbstractTransformation {

    public ReverseTransformation() {
        super(
                "reverse",
                "Reverse Text",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Reverse Mode",
                                ParameterType.TEXT,
                                false,
                                "chars",
                                "What to reverse: chars, words or lines"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        String mode = getStringParam(parameters, "mode", "chars");

        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "words" -> reverseWords(input);
            case "lines" -> reverseLines(input);
            // StringBuilder.reverse keeps surrogate pairs (e.g. emoji) intact
            default -> new StringBuilder(input).reverse().toString();
        };
    }

    private static String reverseWords(String input) {
        String stripped = input.strip();
        if(stripped.isEmpty()) {
            return "";
        }
        List<String> words = Arrays.asList(stripped.split("\\s+"));
        Collections.reverse(words);
        return String.join(" ", words);
    }

    private static String reverseLines(String input) {
        List<String> lines = Arrays.asList(input.split("\\R", -1));
        Collections.reverse(lines);
        return String.join("\n", lines);
    }
}
