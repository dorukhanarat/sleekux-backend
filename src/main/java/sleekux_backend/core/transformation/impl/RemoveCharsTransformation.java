package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;
import sleekux_backend.core.transformation.AbstractTransformation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RemoveCharsTransformation extends AbstractTransformation {

    private static final Map<String, String> CHARACTER_CLASSES = Map.of(
            "digits", "\\p{Nd}",
            "letters", "\\p{L}\\p{M}",
            "punctuation", "\\p{P}",
            "symbols", "\\p{S}",
            "whitespace", "\\s",
            "non_ascii", "[^\\x00-\\x7F]",
            // Pictographs plus the invisible parts of combined emoji (skin tones, joiners, flags and keycaps)
            "emoji", "\\p{IsExtended_Pictographic}\\p{IsEmoji_Modifier}\\x{1F1E6}-\\x{1F1FF}\\x{FE0F}\\x{200D}\\x{20E3}"
    );

    public RemoveCharsTransformation() {
        super(
                "remove_chars",
                "Remove Characters",
                List.of(
                        new ParameterDefinition(
                                "types",
                                "Character Types",
                                ParameterType.TEXT,
                                false,
                                "punctuation",
                                "One or more of digits, letters, punctuation, symbols, whitespace, non_ascii, emoji, e.g. digits,punctuation"
                        )
                )
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }

        String types = getStringParam(parameters, "types", "punctuation");

        Set<String> classes = new LinkedHashSet<>();
        for(String type : types.split(",")) {
            String characterClass = CHARACTER_CLASSES.get(type.strip().toLowerCase(Locale.ROOT));
            if(characterClass != null) {
                classes.add(characterClass);
            }
        }
        // Unknown types are ignored. If none are known, punctuation is removed
        if(classes.isEmpty()) {
            classes.add(CHARACTER_CLASSES.get("punctuation"));
        }

        Pattern pattern = Pattern.compile("(?U)[" + String.join("", classes) + "]");
        return pattern.matcher(input).replaceAll("");
    }
}
