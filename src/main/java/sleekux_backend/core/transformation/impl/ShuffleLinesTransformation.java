package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShuffleLinesTransformation extends AbstractTransformation {

    public ShuffleLinesTransformation() {
        super(
                "shuffle_lines",
                "Shuffle Lines",
                List.of(
                        new ParameterDefinition(
                                "seed",
                                "Seed",
                                ParameterType.NUMBER,
                                false,
                                null,
                                "Optional. The same seed always gives the same order"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null || input.isEmpty()) {
            return "";
        }

        List<String> lines = splitLines(input);
        Collections.shuffle(lines, createRandom(getStringParam(parameters, "seed", "")));
        return String.join("\n", lines);
    }

    // A missing or unreadable seed gives a different order each time
    private static Random createRandom(String seed) {
        try {
            return new Random(Long.parseLong(seed.strip()));
        } catch (NumberFormatException e) {
            return new Random();
        }
    }
}
