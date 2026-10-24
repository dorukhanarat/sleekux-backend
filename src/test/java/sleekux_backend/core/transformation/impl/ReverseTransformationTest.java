package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReverseTransformationTest {

    private ReverseTransformation reverseTransformation;

    @BeforeEach
    void setUp() {
        reverseTransformation = new ReverseTransformation();
    }

    private Map<String, Object> mode(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", value);
        return params;
    }

    // Has key "reverse" and display name "Reverse Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("reverse", reverseTransformation.getKey());
        assertEquals("Reverse Text", reverseTransformation.getDisplayName());
    }

    // Defines an optional mode parameter that defaults to "chars"
    @Test
    void definesModeParameter() {
        List<ParameterDefinition> parameters = reverseTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("chars", parameters.get(0).getDefaultValue());
    }

    // Mode "chars" reverses the characters
    @Test
    void reversesChars() {
        assertEquals("olleH", reverseTransformation.apply("Hello", mode("chars")));
    }

    // Emoji and other surrogate pairs stay intact
    @Test
    void keepsSurrogatePairsIntact() {
        assertEquals("b😀a", reverseTransformation.apply("a😀b", mode("chars")));
    }

    // Mode "words" reverses the word order and collapses whitespace
    @Test
    void reversesWords() {
        assertEquals("three two one", reverseTransformation.apply("  one two   three ", mode("words")));
    }

    // Mode "words" returns an empty string for whitespace-only input
    @Test
    void reversesWordsOfWhitespaceOnly() {
        assertEquals("", reverseTransformation.apply("   ", mode("words")));
    }

    // Mode "lines" reverses the line order and handles CRLF
    @Test
    void reversesLines() {
        assertEquals("c\nb\na", reverseTransformation.apply("a\nb\nc", mode("lines")));
        assertEquals("b\na", reverseTransformation.apply("a\r\nb", mode("lines")));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("b a", reverseTransformation.apply("a b", mode("WORDS")));
    }

    // Reverses characters when the mode is missing, unknown or the map is null
    @Test
    void defaultsToChars() {
        assertEquals("cba", reverseTransformation.apply("abc", new HashMap<>()));
        assertEquals("cba", reverseTransformation.apply("abc", mode("sideways")));
        assertEquals("cba", reverseTransformation.apply("abc", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", reverseTransformation.apply(null, mode("chars")));
    }
}
