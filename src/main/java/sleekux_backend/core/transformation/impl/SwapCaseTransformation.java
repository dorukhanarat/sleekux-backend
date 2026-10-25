package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Map;

public class SwapCaseTransformation extends AbstractTransformation {

    public SwapCaseTransformation() {
        super(
                "swap_case",
                "Swap Case",
                List.of()
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        // Each character is converted on its own, so the result does not depend on the server's language setting
        StringBuilder result = new StringBuilder(input.length());
        input.codePoints().forEach(codePoint -> {
            if(Character.isUpperCase(codePoint) || Character.isTitleCase(codePoint)) {
                result.appendCodePoint(Character.toLowerCase(codePoint));
            } else if(Character.isLowerCase(codePoint)) {
                result.appendCodePoint(Character.toUpperCase(codePoint));
            } else {
                result.appendCodePoint(codePoint);
            }
        });
        return result.toString();
    }
}
