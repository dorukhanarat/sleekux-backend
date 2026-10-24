package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CaseConvertTransformation extends AbstractTransformation {

    private static final Pattern NON_WHITESPACE = Pattern.compile("\\S+");
    private static final Pattern SENTENCE_START = Pattern.compile("(^\\s*|[.!?]\\s+)(\\p{Ll})");

    public CaseConvertTransformation() {
        super(
                "case_convert",
                "Change Case",
                List.of(
                        new ParameterDefinition(
                                "style",
                                "Case Style",
                                ParameterType.TEXT,
                                false,
                                "title",
                                "title, sentence, camel, pascal, snake, kebab or constant"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        String style = getStringParam(parameters, "style", "title");

        return switch (style.toLowerCase(Locale.ROOT)) {
            case "sentence" -> toSentenceCase(input);
            case "camel" -> toCamelCase(input);
            case "pascal" -> joinWords(input, CaseConvertTransformation::capitalize, "");
            case "snake" -> joinWords(input, word -> word.toLowerCase(Locale.ROOT), "_");
            case "kebab" -> joinWords(input, word -> word.toLowerCase(Locale.ROOT), "-");
            case "constant" -> joinWords(input, word -> word.toUpperCase(Locale.ROOT), "_");
            default -> NON_WHITESPACE.matcher(input).replaceAll(match -> capitalize(match.group()));
        };
    }

    private static String toSentenceCase(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return SENTENCE_START.matcher(lower)
                .replaceAll(match -> match.group(1) + match.group(2).toUpperCase(Locale.ROOT));
    }

    private static String toCamelCase(String input) {
        String pascal = joinWords(input, CaseConvertTransformation::capitalize, "");
        if(pascal.isEmpty()) {
            return pascal;
        }
        return pascal.substring(0, 1).toLowerCase(Locale.ROOT) + pascal.substring(1);
    }

    private static String joinWords(String input, Function<String, String> mapper, String separator) {
        return splitWords(input).stream().map(mapper).collect(Collectors.joining(separator));
    }

    // Splits on whitespace, "_" and "-", and between camelCase / PascalCase humps ("parseHTMLText" -> parse, HTML, Text)
    static List<String> splitWords(String input) {
        String spaced = input
                .replaceAll("([\\p{Ll}\\d])(\\p{Lu})", "$1 $2")
                .replaceAll("(\\p{Lu}+)(\\p{Lu}\\p{Ll})", "$1 $2");
        return Arrays.stream(spaced.split("[\\s_\\-]+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }

    private static String capitalize(String word) {
        if(word.isEmpty()) {
            return word;
        }
        int firstLength = Character.charCount(word.codePointAt(0));
        return word.substring(0, firstLength).toUpperCase(Locale.ROOT)
                + word.substring(firstLength).toLowerCase(Locale.ROOT);
    }
}
