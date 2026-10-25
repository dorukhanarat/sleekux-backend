package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlEscapeTransformation extends AbstractTransformation {

    private static final Pattern ENTITY = Pattern.compile("&(#[0-9]+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);");

    private static final Map<String, String> NAMED_ENTITIES = Map.of(
            "amp", "&",
            "lt", "<",
            "gt", ">",
            "quot", "\"",
            "apos", "'",
            "nbsp", String.valueOf((char) 0xA0)
    );

    public HtmlEscapeTransformation() {
        super(
                "html_escape",
                "Escape HTML",
                List.of(
                        new ParameterDefinition(
                                "mode",
                                "Mode",
                                ParameterType.TEXT,
                                false,
                                "escape",
                                "escape turns <b> into &lt;b&gt;, unescape turns it back"
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

    private String escape(String input) {
        StringBuilder result = new StringBuilder(input.length());
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> result.append("&amp;");
                case '<' -> result.append("&lt;");
                case '>' -> result.append("&gt;");
                case '"' -> result.append("&quot;");
                case '\'' -> result.append("&#39;");
                default -> result.append(c);
            }
        }
        return result.toString();
    }

    // Entities are replaced in a single pass, so "&amp;lt;" becomes "&lt;" and not "<".
    // Unknown entities are left as they are
    static String unescape(String input) {
        Matcher matcher = ENTITY.matcher(input);
        return matcher.replaceAll(match -> Matcher.quoteReplacement(decodeEntity(match.group(1), match.group())));
    }

    private static String decodeEntity(String entity, String original) {
        if(!entity.startsWith("#")) {
            return NAMED_ENTITIES.getOrDefault(entity, original);
        }

        try {
            boolean hex = entity.length() > 1 && (entity.charAt(1) == 'x' || entity.charAt(1) == 'X');
            int codePoint = hex ? Integer.parseInt(entity.substring(2), 16) : Integer.parseInt(entity.substring(1));
            return Character.isValidCodePoint(codePoint) ? Character.toString(codePoint) : original;
        } catch (NumberFormatException e) {
            return original;
        }
    }
}
