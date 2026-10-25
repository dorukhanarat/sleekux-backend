package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
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

    private Map<String, Object> params(Object oldValue, Object newValue, Object caseInsensitive) {
        Map<String, Object> params = params(oldValue, newValue);
        params.put("caseInsensitive", caseInsensitive);
        return params;
    }

    // Has key "replace" and display name "Replace Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("replace", replaceTransformation.getKey());
        assertEquals("Replace Text", replaceTransformation.getDisplayName());
    }

    // Defines a required oldValue, an optional newValue and an optional caseInsensitive flag
    @Test
    void definesOldValueAndNewValueParameters() {
        List<ParameterDefinition> parameters = replaceTransformation.getParameters();

        assertEquals(3, parameters.size());

        ParameterDefinition caseInsensitive = parameters.get(2);
        assertEquals("caseInsensitive", caseInsensitive.getName());
        assertEquals(ParameterType.BOOLEAN, caseInsensitive.getType());
        assertFalse(caseInsensitive.isRequired());
        assertEquals(false, caseInsensitive.getDefaultValue());

        ParameterDefinition oldValue = parameters.get(0);
        assertEquals("oldValue", oldValue.getName());
        assertEquals(ParameterType.TEXT, oldValue.getType());
        assertTrue(oldValue.isRequired());
        assertEquals("Exact text to find, e.g. world", oldValue.getPlaceholder());

        ParameterDefinition newValue = parameters.get(1);
        assertEquals("newValue", newValue.getName());
        assertEquals(ParameterType.TEXT, newValue.getType());
        assertFalse(newValue.isRequired());
    }

    // Replaces a single occurrence
    @Test
    void replacesSingleOccurrence() {
        assertEquals("Hello Java", replaceTransformation.apply("Hello World", params("World", "Java")));
    }

    // Replaces every occurrence
    @Test
    void replacesAllOccurrences() {
        assertEquals("a+b+c", replaceTransformation.apply("a-b-c", params("-", "+")));
    }

    // Returns the input unchanged when there is no match
    @Test
    void returnsInputUnchangedWhenNoMatch() {
        assertEquals("Hello World", replaceTransformation.apply("Hello World", params("xyz", "abc")));
    }

    // Matching is case-sensitive
    @Test
    void isCaseSensitive() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params("hello", "bye")));
    }

    // Regex special characters are matched literally
    @Test
    void treatsOldValueLiterally() {
        assertEquals("a-b-c", replaceTransformation.apply("a.b.c", params(".", "-")));
        assertEquals("1-1", replaceTransformation.apply("1+1", params("+", "-")));
    }

    // Removes matches when newValue is missing
    @Test
    void removesMatchesWhenNewValueIsMissing() {
        Map<String, Object> params = new HashMap<>();
        params.put("oldValue", "l");

        assertEquals("Heo Word", replaceTransformation.apply("Hello World", params));
    }

    // Removes matches when newValue is null
    @Test
    void removesMatchesWhenNewValueIsNull() {
        assertEquals("Heo Word", replaceTransformation.apply("Hello World", params("l", null)));
    }

    // Returns the input unchanged when oldValue is missing
    @Test
    void returnsInputUnchangedWhenOldValueIsMissing() {
        Map<String, Object> params = new HashMap<>();
        params.put("newValue", "x");

        assertEquals("Hello", replaceTransformation.apply("Hello", params));
    }

    // Returns the input unchanged when oldValue is null
    @Test
    void returnsInputUnchangedWhenOldValueIsNull() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params(null, "x")));
    }

    // Returns the input unchanged when oldValue is empty
    @Test
    void returnsInputUnchangedWhenOldValueIsEmpty() {
        assertEquals("Hello", replaceTransformation.apply("Hello", params("", "x")));
    }

    // Returns the input unchanged when the parameter map is null or empty
    @Test
    void returnsInputUnchangedWhenParametersAreNullOrEmpty() {
        assertEquals("Hello", replaceTransformation.apply("Hello", null));
        assertEquals("Hello", replaceTransformation.apply("Hello", new HashMap<>()));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", replaceTransformation.apply(null, params("a", "b")));
    }

    // Returns an empty string for empty input
    @Test
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", replaceTransformation.apply("", params("a", "b")));
    }

    // Converts non-string parameter values to text
    @Test
    void convertsNonStringParametersToString() {
        assertEquals("202", replaceTransformation.apply("101", params(1, 2)));
    }

    // Replaces non-overlapping matches from left to right
    @Test
    void replacesNonOverlappingMatchesFromLeft() {
        assertEquals("ba", replaceTransformation.apply("aaa", params("aa", "b")));
    }

    // With caseInsensitive, matches regardless of upper and lower case
    @Test
    void caseInsensitiveMatchesAnyCase() {
        assertEquals("bye bye bye", replaceTransformation.apply("Hello hello HELLO", params("hello", "bye", true)));
    }

    // With caseInsensitive, also matches non-ASCII letters in any case
    @Test
    void caseInsensitiveHandlesNonAscii() {
        assertEquals("tea tea", replaceTransformation.apply("Çay ÇAY", params("çay", "tea", true)));
        assertEquals("x x", replaceTransformation.apply("Ölçü ÖLÇÜ", params("ölçü", "x", true)));
    }

    // With caseInsensitive, regex characters in both values are still treated literally
    @Test
    void caseInsensitiveIsStillLiteral() {
        assertEquals("$1 costs $1", replaceTransformation.apply("A.B costs a.b", params("a.b", "$1", true)));
        assertEquals("a\\b", replaceTransformation.apply("aXb", params("x", "\\", true)));
    }

    // Matching is case-sensitive when caseInsensitive is false, missing or not a valid boolean
    @Test
    void caseSensitiveByDefault() {
        assertEquals("Hello bye", replaceTransformation.apply("Hello hello", params("hello", "bye", false)));
        assertEquals("Hello bye", replaceTransformation.apply("Hello hello", params("hello", "bye")));
        assertEquals("Hello bye", replaceTransformation.apply("Hello hello", params("hello", "bye", "maybe")));
    }
}
