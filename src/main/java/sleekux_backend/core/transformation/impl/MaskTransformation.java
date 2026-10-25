package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class MaskTransformation extends AbstractTransformation {

    public MaskTransformation() {
        super(
                "mask",
                "Mask Text",
                List.of(
                        new ParameterDefinition(
                                "visibleStart",
                                "Visible at Start",
                                ParameterType.NUMBER,
                                false,
                                0,
                                "Letters and digits to leave visible at the start"
                        ),
                        new ParameterDefinition(
                                "visibleEnd",
                                "Visible at End",
                                ParameterType.NUMBER,
                                false,
                                4,
                                "Letters and digits to leave visible at the end, e.g. 4 for card numbers"
                        ),
                        new ParameterDefinition(
                                "maskChar",
                                "Mask Character",
                                ParameterType.TEXT,
                                false,
                                "*",
                                "A single character, default is *"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        int visibleStart = Math.max(0, getIntParam(parameters, "visibleStart", 0));
        int visibleEnd = Math.max(0, getIntParam(parameters, "visibleEnd", 4));
        String maskChar = getStringParam(parameters, "maskChar", "*");
        // Only the first character is used. An empty value falls back to *
        String mask = maskChar.isEmpty() ? "*" : maskChar.substring(0, maskChar.offsetByCodePoints(0, 1));

        // Only letters and digits are masked, so separators like spaces, dashes and @ keep the shape readable
        long maskable = input.codePoints().filter(Character::isLetterOrDigit).count();
        if(visibleStart + visibleEnd >= maskable) {
            return input;
        }

        StringBuilder result = new StringBuilder(input.length());
        int position = 0;
        for(int codePoint : input.codePoints().toArray()) {
            if(!Character.isLetterOrDigit(codePoint)) {
                result.appendCodePoint(codePoint);
                continue;
            }
            boolean visible = position < visibleStart || position >= maskable - visibleEnd;
            if(visible) {
                result.appendCodePoint(codePoint);
            } else {
                result.append(mask);
            }
            position++;
        }
        return result.toString();
    }
}
