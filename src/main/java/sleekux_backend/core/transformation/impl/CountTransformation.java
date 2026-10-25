package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class CountTransformation extends AbstractTransformation {

    private static final Pattern WORD = Pattern.compile("(?U)\\S+");

    public CountTransformation() {
        super(
                "count",
                "Count",
                List.of(
                        new ParameterDefinition(
                                "what",
                                "What to Count",
                                ParameterType.TEXT,
                                false,
                                "all",
                                "chars, words, lines or all"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        String text = input != null ? input : "";
        String what = getStringParam(parameters, "what", "all");

        return switch (what.toLowerCase(Locale.ROOT)) {
            case "chars" -> String.valueOf(countChars(text));
            case "words" -> String.valueOf(countWords(text));
            case "lines" -> String.valueOf(countLines(text));
            default -> "chars: " + countChars(text) + "\nwords: " + countWords(text) + "\nlines: " + countLines(text);
        };
    }

    // Counted in code points, so an emoji is one character
    private static int countChars(String text) {
        return text.codePointCount(0, text.length());
    }

    private static long countWords(String text) {
        return WORD.matcher(text).results().count();
    }

    // Empty text has no lines, and a line break at the very end does not start a new line
    private static int countLines(String text) {
        return text.isEmpty() ? 0 : splitLines(text).size();
    }
}
