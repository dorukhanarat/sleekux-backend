package sleekux_backend.core.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReplaceTransformationTest {

    private ReplaceTransformation replaceTransformation;

    @BeforeEach
    void setUp() {
        replaceTransformation = new ReplaceTransformation();
    }

    private Map<String, Object> params(Object oldValue, Object newValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("oldValue", oldValue);
        params.put("newValue", newValue);
        return params;
    }

    @Test
    @DisplayName("Has key \"replace\" and display name \"Replace Text\"")
    void hasExpectedKeyAndDisplayName() {
        assertEquals("replace", replaceTransformation.getKey());
        assertEquals("Replace Text", replaceTransformation.getDisplayName());
    }

    @Test
    @DisplayName("Defines a required oldValue and an optional newValue text parameter")
    void definesOldValueAndNewValueParameters() {
        List<ParameterDefinition> parameters = replaceTransformation.getParameters();

        assertEquals(2, parameters.size());

        ParameterDefinition oldValue = parameters.get(0);
        assertEquals("oldValue", oldValue.getName());
        assertEquals(ParameterType.TEXT, oldValue.getType());
        assertTrue(oldValue.isRequired());
        assertEquals("Text to replace", oldValue.getPlaceholder());

        ParameterDefinition newValue = parameters.get(1);
        assertEquals("newValue", newValue.getName());
        assertEquals(ParameterType.TEXT, newValue.getType());
        assertFalse(newValue.isRequired());
    }

    @Test
    @DisplayName("Replaces a single occurrence")
    void replacesSingleOccurrence() {
        assertEquals("Hello Java", replaceTransformation.apply("Hello World", params("World", "Java")));
    }

    @Test
    @DisplayName("Replaces every occurrence")
    void replacesAllOccurrences() {
        assertEquals("a+b+c", replaceTransformation.apply("a-b-c", params("-", "+")));
    }

    @Test
    @DisplayName("Returns the input unchanged when there is no match")
    void returnsInputUnchangedWhenNoMatch() {
        assertEquals("Hello World", replaceTransformation.apply("Hello World", params("xyz", "abc")));
    }

    @Test
    @DisplayName("Matching is case-sensitive")
    void isCaseSensitive() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params("hello", "bye")));
    }

    @Test
    @DisplayName("Regex special characters are matched literally")
    void treatsOldValueLiterally() {
        assertEquals("a-b-c", replaceTransformation.apply("a.b.c", params(".", "-")));
        assertEquals("1-1", replaceTransformation.apply("1+1", params("+", "-")));
    }

    @Test
    @DisplayName("Removes matches when newValue is missing")
    void removesMatchesWhenNewValueIsMissing() {
        Map<String, Object> params = new HashMap<>();
        params.put("oldValue", "l");

        assertEquals("Heo Word", replaceTransformation.apply("Hello World", params));
    }

    @Test
    @DisplayName("Removes matches when newValue is null")
    void removesMatchesWhenNewValueIsNull() {
        assertEquals("Heo Word", replaceTransformation.apply("Hello World", params("l", null)));
    }

    @Test
    @DisplayName("Returns the input unchanged when oldValue is missing")
    void returnsInputUnchangedWhenOldValueIsMissing() {
        Map<String, Object> params = new HashMap<>();
        params.put("newValue", "x");

        assertEquals("Hello", replaceTransformation.apply("Hello", params));
    }

    @Test
    @DisplayName("Returns the input unchanged when oldValue is null")
    void returnsInputUnchangedWhenOldValueIsNull() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params(null, "x")));
    }

    @Test
    @DisplayName("Returns the input unchanged when oldValue is empty")
    void returnsInputUnchangedWhenOldValueIsEmpty() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params("", "x")));
    }

    @Test
    @DisplayName("Returns the input unchanged when the parameter map is null or empty")
    void returnsInputUnchangedWhenParametersAreNullOrEmpty() {
        assertEquals("Hello", replaceTransformation.apply("Hello", null));
        assertEquals("Hello", replaceTransformation.apply("Hello", new HashMap<>()));
    }

    @Test
    @DisplayName("Returns an empty string for null input")
    void returnsEmptyStringForNullInput() {
        assertEquals("", replaceTransformation.apply(null, params("a", "b")));
    }

    @Test
    @DisplayName("Returns an empty string for empty input")
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", replaceTransformation.apply("", params("a", "b")));
    }

    @Test
    @DisplayName("Converts non-string parameter values to text")
    void convertsNonStringParametersToString() {
        assertEquals("202", replaceTransformation.apply("101", params(1, 2)));
    }

    @Test
    @DisplayName("Replaces non-overlapping matches from left to right")
    void replacesNonOverlappingMatchesFromLeft() {
        assertEquals("ba", replaceTransformation.apply("aaa", params("aa", "b")));
    }
}
