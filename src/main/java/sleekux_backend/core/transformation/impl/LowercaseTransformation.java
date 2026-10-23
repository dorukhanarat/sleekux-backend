package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LowercaseTransformation extends AbstractTransformation {

    public LowercaseTransformation() {
        super(
                "lowercase",
                "To Lowercase",
                List.of()
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT);
    }
}
