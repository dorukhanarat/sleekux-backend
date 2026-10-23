package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UppercaseTransformationTest {

    private UppercaseTransformation uppercaseTransformation;

    @BeforeEach
    void setUp() {
        uppercaseTransformation = new UppercaseTransformation();
    }

    // Has key "uppercase" and display name "To Uppercase"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("uppercase", uppercaseTransformation.getKey());
        assertEquals("To Uppercase", uppercaseTransformation.getDisplayName());
    }

    // Has no parameters
    @Test
    void hasNoParameters() {
        assertTrue(uppercaseTransformation.getParameters().isEmpty());
    }

    // Converts all letters to uppercase
    @Test
    void convertsToUppercase() {
        assertEquals("HELLO WORLD", uppercaseTransformation.apply("Hello World", Map.of()));
    }

    // Leaves digits, symbols and already uppercase text unchanged
    @Test
    void leavesNonLowercaseCharactersUnchanged() {
        assertEquals("ABC 123 !?", uppercaseTransformation.apply("ABC 123 !?", Map.of()));
    }

    // Ignores the parameter map, even when it is null
    @Test
    void ignoresParameters() {
        assertEquals("ABC", uppercaseTransformation.apply("abc", null));
        assertEquals("ABC", uppercaseTransformation.apply("abc", Map.of("unused", "value")));
    }

    // Gives the same result regardless of the default locale
    @Test
    void isLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("TITLE", uppercaseTransformation.apply("title", Map.of()));
        } finally {
            Locale.setDefault(original);
        }
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", uppercaseTransformation.apply(null, Map.of()));
    }

    // Returns an empty string for empty input
    @Test
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", uppercaseTransformation.apply("", Map.of()));
    }
}
