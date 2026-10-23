package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UppercaseTransformation extends AbstractTransformation {

    public UppercaseTransformation() {
        super(
                "uppercase",
                "To Uppercase",
                List.of()
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        return input.toUpperCase(Locale.ROOT);
    }
}
