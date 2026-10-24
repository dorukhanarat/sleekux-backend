package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PadTransformationTest {

    private PadTransformation padTransformation;

    @BeforeEach
    void setUp() {
        padTransformation = new PadTransformation();
    }

    private Map<String, Object> params(Object length, Object padChar, Object side) {
        Map<String, Object> params = new HashMap<>();
        params.put("length", length);
        params.put("padChar", padChar);
        params.put("side", side);
        return params;
    }

    // Has key "pad" and display name "Pad Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("pad", padTransformation.getKey());
        assertEquals("Pad Text", padTransformation.getDisplayName());
    }

    // Defines a required length, an optional padChar and an optional side
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = padTransformation.getParameters();

        assertEquals(3, parameters.size());
        assertEquals("length", parameters.get(0).getName());
        assertEquals(ParameterType.NUMBER, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
        assertEquals(0, parameters.get(0).getDefaultValue());
        assertEquals("padChar", parameters.get(1).getName());
        assertEquals(" ", parameters.get(1).getDefaultValue());
        assertEquals("side", parameters.get(2).getName());
        assertEquals("start", parameters.get(2).getDefaultValue());
    }

    // Side "start" pads on the left
    @Test
    void padsStart() {
        assertEquals("00042", padTransformation.apply("42", params(5, "0", "start")));
    }

    // Side "end" pads on the right
    @Test
    void padsEnd() {
        assertEquals("ab...", padTransformation.apply("ab", params(5, ".", "end")));
    }

    // Side "both" pads both sides, with the extra character on the right
    @Test
    void padsBoth() {
        assertEquals("**ab***", padTransformation.apply("ab", params(7, "*", "both")));
    }

    // Side is case-insensitive
    @Test
    void sideIsCaseInsensitive() {
        assertEquals("ab..", padTransformation.apply("ab", params(4, ".", "END")));
    }

    // Pads the start with spaces by default
    @Test
    void defaultsToSpacesAtStart() {
        Map<String, Object> params = new HashMap<>();
        params.put("length", 4);

        assertEquals("  ab", padTransformation.apply("ab", params));
        assertEquals("  ab", padTransformation.apply("ab", params(4, null, "middle")));
    }

    // Only the first character of padChar is used, and an empty padChar means a space
    @Test
    void usesFirstPadCharacter() {
        assertEquals("xxab", padTransformation.apply("ab", params(4, "xy", "start")));
        assertEquals("  ab", padTransformation.apply("ab", params(4, "", "start")));
    }

    // Text that is already long enough is returned unchanged
    @Test
    void leavesLongTextUnchanged() {
        assertEquals("hello", padTransformation.apply("hello", params(3, "0", "start")));
        assertEquals("hello", padTransformation.apply("hello", params(-1, "0", "start")));
        assertEquals("hello", padTransformation.apply("hello", null));
    }

    // Length can be sent as a string
    @Test
    void acceptsLengthAsString() {
        assertEquals("00042", padTransformation.apply("42", params("5", "0", "start")));
    }

    // Length is capped to avoid huge results
    @Test
    void capsLength() {
        assertEquals(PadTransformation.MAX_LENGTH, padTransformation.apply("a", params(50_000, "-", "start")).length());
    }

    // An emoji counts as one character
    @Test
    void countsEmojiAsOneCharacter() {
        assertEquals("--😀", padTransformation.apply("😀", params(3, "-", "start")));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", padTransformation.apply(null, params(5, "0", "start")));
    }
}
