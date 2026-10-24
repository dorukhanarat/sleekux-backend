package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PadTransformation extends AbstractTransformation {

    static final int MAX_LENGTH = 10_000;

    public PadTransformation() {
        super(
                "pad",
                "Pad Text",
                List.of(
                        new ParameterDefinition(
                                "length",
                                "Target Length",
                                ParameterType.NUMBER,
                                true,
                                0,
                                "Minimum length of the result, e.g. 5 turns 42 into 00042"
                        ),
                        new ParameterDefinition(
                                "padChar",
                                "Pad Character",
                                ParameterType.TEXT,
                                false,
                                " ",
                                "A single character, e.g. 0, default is a space"
                        ),
                        new ParameterDefinition(
                                "side",
                                "Pad Side",
                                ParameterType.TEXT,
                                false,
                                "start",
                                "Where to pad: start, end or both"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        int length = Math.min(getIntParam(parameters, "length", 0), MAX_LENGTH);
        int missing = length - input.codePointCount(0, input.length());
        if(missing <= 0) {
            return input;
        }

        String padChar = getStringParam(parameters, "padChar", " ");
        // Only the first character is used. An empty value falls back to a space
        String pad = padChar.isEmpty() ? " " : padChar.substring(0, padChar.offsetByCodePoints(0, 1));
        String side = getStringParam(parameters, "side", "start");

        return switch (side.toLowerCase(Locale.ROOT)) {
            case "end" -> input + pad.repeat(missing);
            case "both" -> {
                int left = missing / 2;
                yield pad.repeat(left) + input + pad.repeat(missing - left);
            }
            default -> pad.repeat(missing) + input;
        };
    }
}
