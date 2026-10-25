package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonEscapeTransformation extends AbstractTransformation {

    public JsonEscapeTransformation() {
        super(
                "json_escape",
                "Escape JSON String",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "escape",
                                "escape turns a line break into \\n and \" into \\\", unescape turns them back"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String mode = getStringParam(parameters, "mode", "escape");

        return mode.toLowerCase(Locale.ROOT).equals("unescape") ? unescape(input) : escape(input);
    }

    // Produces text that can be pasted between the quotes of a JSON or Java string
    private static String escape(String input) {
        StringBuilder result = new StringBuilder(input.length() + 16);
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                default -> {
                    if(c < 0x20) {
                        // Other control characters have no short form
                        result.append('\\').append('u').append(HexFormat.of().toHexDigits(c));
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        return result.toString();
    }

    private static String unescape(String input) {
        StringBuilder result = new StringBuilder(input.length());
        int i = 0;
        while(i < input.length()) {
            char c = input.charAt(i);
            if(c != '\\') {
                result.append(c);
                i++;
                continue;
            }

            if(i + 1 >= input.length()) {
                throw new IllegalArgumentException("Invalid escape sequence: text ends with a single \\");
            }

            char next = input.charAt(i + 1);
            switch (next) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'u' -> {
                    result.append(parseHexChar(input, i));
                    i += 4;
                }
                default -> throw new IllegalArgumentException(
                        "Invalid escape sequence \\" + next + " at position " + (i + 1));
            }
            i += 2;
        }
        return result.toString();
    }

    private static char parseHexChar(String input, int backslashIndex) {
        int start = backslashIndex + 2;
        int end = start + 4;
        if(end > input.length()) {
            throw new IllegalArgumentException("Invalid escape sequence: \\u needs 4 hex digits at position " + (backslashIndex + 1));
        }
        String hex = input.substring(start, end);
        if(!hex.chars().allMatch(ch -> Character.digit(ch, 16) >= 0)) {
            throw new IllegalArgumentException("Invalid escape sequence: \\u needs 4 hex digits at position " + (backslashIndex + 1));
        }
        return (char) Integer.parseInt(hex, 16);
    }
}
