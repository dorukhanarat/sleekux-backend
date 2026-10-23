package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LowercaseTransformationTest {

    private LowercaseTransformation lowercaseTransformation;

    @BeforeEach
    void setUp() {
        lowercaseTransformation = new LowercaseTransformation();
    }

    // Has key "lowercase" and display name "To Lowercase"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("lowercase", lowercaseTransformation.getKey());
        assertEquals("To Lowercase", lowercaseTransformation.getDisplayName());
    }

    // Has no parameters
    @Test
    void hasNoParameters() {
        assertTrue(lowercaseTransformation.getParameters().isEmpty());
    }

    // Converts all letters to lowercase
    @Test
    void convertsToLowercase() {
        assertEquals("hello world", lowercaseTransformation.apply("Hello WORLD", Map.of()));
    }

    // Leaves digits, symbols and already lowercase text unchanged
    @Test
    void leavesNonUppercaseCharactersUnchanged() {
        assertEquals("abc 123 !?", lowercaseTransformation.apply("abc 123 !?", Map.of()));
    }

    // Ignores the parameter map, even when it is null
    @Test
    void ignoresParameters() {
        assertEquals("abc", lowercaseTransformation.apply("ABC", null));
        assertEquals("abc", lowercaseTransformation.apply("ABC", Map.of("unused", "value")));
    }

    // Gives the same result regardless of the default locale
    @Test
    void isLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("title", lowercaseTransformation.apply("TITLE", Map.of()));
        } finally {
            Locale.setDefault(original);
        }
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", lowercaseTransformation.apply(null, Map.of()));
    }

    // Returns an empty string for empty input
    @Test
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", lowercaseTransformation.apply("", Map.of()));
    }
}
