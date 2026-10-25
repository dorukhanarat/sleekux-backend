package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TruncateTransformationTest {

    private TruncateTransformation truncateTransformation;

    @BeforeEach
    void setUp() {
        truncateTransformation = new TruncateTransformation();
    }

    private Map<String, Object> params(Object length, Object ellipsis, Object wordBoundary) {
        Map<String, Object> params = new HashMap<>();
        params.put("length", length);
        params.put("ellipsis", ellipsis);
        params.put("wordBoundary", wordBoundary);
        return params;
    }

    // Has key "truncate" and display name "Truncate Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("truncate", truncateTransformation.getKey());
        assertEquals("Truncate Text", truncateTransformation.getDisplayName());
    }

    // Defines a required length (default 100), an optional ellipsis (default "…") and an optional wordBoundary (default true)
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = truncateTransformation.getParameters();

        assertEquals(3, parameters.size());
        assertEquals("length", parameters.get(0).getName());
        assertEquals(ParameterType.NUMBER, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
        assertEquals(100, parameters.get(0).getDefaultValue());
        assertEquals("ellipsis", parameters.get(1).getName());
        assertEquals("…", parameters.get(1).getDefaultValue());
        assertEquals("wordBoundary", parameters.get(2).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(2).getType());
        assertEquals(true, parameters.get(2).getDefaultValue());
    }

    // Returns text that is not longer than the limit unchanged, without an ellipsis
    @Test
    void keepsShortText() {
        assertEquals("Hello", truncateTransformation.apply("Hello", params(10, "…", true)));
        assertEquals("Hello", truncateTransformation.apply("Hello", params(5, "…", true)));
    }

    // Cuts at the last space so no word is split, and the result fits within the limit
    @Test
    void cutsAtWordBoundary() {
        String result = truncateTransformation.apply("The quick brown fox", params(12, "…", true));

        assertEquals("The quick…", result);
        assertTrue(result.length() <= 12);
    }

    // Keeps the last word when the cut falls exactly on a space
    @Test
    void keepsWordWhenCutIsAtSpace() {
        assertEquals("The quick…", truncateTransformation.apply("The quick brown fox", params(10, "…", true)));
    }

    // Cuts in the middle of a word when wordBoundary is false
    @Test
    void cutsMidWordWhenDisabled() {
        assertEquals("The quick b…", truncateTransformation.apply("The quick brown fox", params(12, "…", false)));
    }

    // Cuts in the middle of a word when there is no space to cut at
    @Test
    void cutsMidWordWhenNoSpace() {
        assertEquals("Supercali…", truncateTransformation.apply("Supercalifragilistic", params(10, "…", true)));
    }

    // Removes whitespace left before the ellipsis
    @Test
    void removesTrailingWhitespaceBeforeEllipsis() {
        assertEquals("Hello…", truncateTransformation.apply("Hello     world", params(8, "…", false)));
    }

    // Uses a custom ellipsis and counts it towards the length
    @Test
    void usesCustomEllipsis() {
        assertEquals("The quick...", truncateTransformation.apply("The quick brown fox", params(12, "...", true)));
        assertEquals("The quick brown", truncateTransformation.apply("The quick brown fox", params(16, "", true)));
    }

    // When the ellipsis does not fit, returns as much of the ellipsis as fits
    @Test
    void ellipsisLongerThanLength() {
        assertEquals("..", truncateTransformation.apply("Hello world", params(2, "...", true)));
    }

    // A length of zero or less returns an empty string
    @Test
    void zeroOrNegativeLength() {
        assertEquals("", truncateTransformation.apply("Hello", params(0, "…", true)));
        assertEquals("", truncateTransformation.apply("Hello", params(-5, "…", true)));
    }

    // Counts emoji as one character and never cuts them in half
    @Test
    void countsEmojiAsOneCharacter() {
        assertEquals("😀😀😀", truncateTransformation.apply("😀😀😀", params(3, "…", false)));
        assertEquals("😀😀…", truncateTransformation.apply("😀😀😀😀", params(3, "…", false)));
    }

    // Uses length 100, "…" and word boundaries when parameters are missing
    @Test
    void usesDefaults() {
        String longText = "word ".repeat(30).strip();

        String result = truncateTransformation.apply(longText, new HashMap<>());

        assertTrue(result.endsWith("word…"));
        assertTrue(result.codePointCount(0, result.length()) <= 100);
        assertEquals(result, truncateTransformation.apply(longText, null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", truncateTransformation.apply(null, params(10, "…", true)));
    }
}
