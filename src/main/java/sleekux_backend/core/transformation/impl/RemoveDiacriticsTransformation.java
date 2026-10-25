package sleekux_backend.core.transformation.impl;

import sleekux_backend.core.transformation.AbstractTransformation;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RemoveDiacriticsTransformation extends AbstractTransformation {

    // Only marks from the Latin/Greek/Cyrillic accent block are removed, so marks that are part of
    // other scripts (e.g. Devanagari vowel signs) are kept
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    // Letters that Unicode does not split into a base letter and an accent
    private static final Map<String, String> SPECIAL_LETTERS = Map.ofEntries(
            Map.entry("ı", "i"),
            Map.entry("ł", "l"), Map.entry("Ł", "L"),
            Map.entry("ø", "o"), Map.entry("Ø", "O"),
            Map.entry("đ", "d"), Map.entry("Đ", "D"),
            Map.entry("ħ", "h"), Map.entry("Ħ", "H"),
            Map.entry("ß", "ss"),
            Map.entry("æ", "ae"), Map.entry("Æ", "AE"),
            Map.entry("œ", "oe"), Map.entry("Œ", "OE"),
            Map.entry("þ", "th"), Map.entry("Þ", "TH")
    );

    public RemoveDiacriticsTransformation() {
        super(
                "remove_diacritics",
                "Remove Accents",
                List.of()
        );
    }

    @Override
    public String apply(String input, Map<String, Object> parameters) {
        if(input == null) {
            return "";
        }
        return removeDiacritics(input);
    }

    static String removeDiacritics(String input) {
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("");

        StringBuilder result = new StringBuilder(withoutMarks.length());
        withoutMarks.codePoints().forEach(codePoint -> {
            String letter = Character.toString(codePoint);
            result.append(SPECIAL_LETTERS.getOrDefault(letter, letter));
        });

        // Recompose so that scripts which NFD splits apart (e.g. Korean Hangul) come back unchanged
        return Normalizer.normalize(result, Normalizer.Form.NFC);
    }
}
