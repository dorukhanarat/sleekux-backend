package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrimTransformation extends AbstractTransformation {

    public TrimTransformation() {
        super(
                "trim",
                "Trim Whitespace",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Trim Mode",
                                ParameterType.TEXT,
                                false,
                                "both",
                                "Where to trim: start, end or both"
                        ),
                        new ParameterDefinition(
                                "chars",
                                "Characters to Trim",
                                ParameterType.TEXT,
                                false,
                                "",
                                "e.g. /\" trims slashes and quotes, leave empty for whitespace"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        String mode = getStringParam(parameters, "mode", "both");
        String chars = getStringParam(parameters, "chars", "");

        boolean trimStart = !mode.toLowerCase(Locale.ROOT).equals("end");
        boolean trimEnd = !mode.toLowerCase(Locale.ROOT).equals("start");

        if(chars.isEmpty()) {
            String result = trimStart ? input.stripLeading() : input;
            return trimEnd ? result.stripTrailing() : result;
        }

        return trimChars(input, chars, trimStart, trimEnd);
    }

    // Removes any of the given characters from the ends, counting by code point so emoji can be trimmed too
    private static String trimChars(String input, String chars, boolean trimStart, boolean trimEnd) {
        int start = 0;
        int end = input.length();

        while(trimStart && start < end && chars.indexOf(input.codePointAt(start)) >= 0) {
            start += Character.charCount(input.codePointAt(start));
        }
        while(trimEnd && end > start && chars.indexOf(input.codePointBefore(end)) >= 0) {
            end -= Character.charCount(input.codePointBefore(end));
        }

        return input.substring(start, end);
    }
}
