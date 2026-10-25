package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class TruncateTransformation extends AbstractTransformation {

    public TruncateTransformation() {
        super(
                "truncate",
                "Truncate Text",
                List.of(
                        new ParameterDefinition(
                                "length",
                                "Maximum Length",
                                ParameterType.NUMBER,
                                true,
                                100,
                                "Maximum length of the result, including the ending"
                        ),
                        new ParameterDefinition(
                                "ellipsis",
                                "Ending",
                                ParameterType.TEXT,
                                false,
                                "…",
                                "Added when the text is cut, e.g. … or (more)"
                        ),
                        new ParameterDefinition(
                                "wordBoundary",
                                "Keep Whole Words",
                                ParameterType.BOOLEAN,
                                false,
                                true,
                                "Cut at the last space instead of in the middle of a word"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        int length = Math.max(0, getIntParam(parameters, "length", 100));
        String ellipsis = getStringParam(parameters, "ellipsis", "…");
        boolean wordBoundary = getBooleanParam(parameters, "wordBoundary", true);

        // Lengths are counted in code points, so emoji are never cut in half
        if(input.codePointCount(0, input.length()) <= length) {
            return input;
        }

        int ellipsisLength = ellipsis.codePointCount(0, ellipsis.length());
        if(ellipsisLength >= length) {
            return firstCodePoints(ellipsis, length);
        }

        String kept = firstCodePoints(input, length - ellipsisLength);

        if(wordBoundary) {
            boolean cutInsideWord = !Character.isWhitespace(input.codePointAt(kept.length()));
            int lastSpace = lastWhitespace(kept);
            if(cutInsideWord && lastSpace > 0) {
                kept = kept.substring(0, lastSpace);
            }
        }

        return kept.stripTrailing() + ellipsis;
    }

    private static String firstCodePoints(String text, int count) {
        return text.substring(0, text.offsetByCodePoints(0, count));
    }

    private static int lastWhitespace(String text) {
        for(int i = text.length() - 1; i >= 0; i--) {
            if(Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
