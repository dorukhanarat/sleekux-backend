package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SubstringTransformationTest {

    private SubstringTransformation substringTransformation;

    @BeforeEach
    void setUp() {
        substringTransformation = new SubstringTransformation();
    }

    private Map<String, Object> range(Object startIndex, Object endIndex) {
        Map<String, Object> params = new HashMap<>();
        params.put("startIndex", startIndex);
        params.put("endIndex", endIndex);
        return params;
    }

    // Has key "substring" and display name "Substring"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("substring", substringTransformation.getKey());
        assertEquals("Substring", substringTransformation.getDisplayName());
    }

    // Defines a required startIndex (default 0) and an optional endIndex (default -1)
    @Test
    void definesIndexParameters() {
        List<ParameterDefinition> parameters = substringTransformation.getParameters();

        assertEquals(2, parameters.size());

        ParameterDefinition start = parameters.get(0);
        assertEquals("startIndex", start.getName());
        assertEquals(ParameterType.NUMBER, start.getType());
        assertTrue(start.isRequired());
        assertEquals(0, start.getDefaultValue());

        ParameterDefinition end = parameters.get(1);
        assertEquals("endIndex", end.getName());
        assertEquals(ParameterType.NUMBER, end.getType());
        assertFalse(end.isRequired());
        assertEquals(-1, end.getDefaultValue());
    }

    // Returns the text between startIndex (inclusive) and endIndex (exclusive)
    @Test
    void returnsRange() {
        assertEquals("ell", substringTransformation.apply("Hello", range(1, 4)));
    }

    // Returns the whole text for the full range
    @Test
    void returnsWholeTextForFullRange() {
        assertEquals("Hello", substringTransformation.apply("Hello", range(0, 5)));
    }

    // Accepts indexes given as numeric strings
    @Test
    void acceptsNumericStrings() {
        assertEquals("ell", substringTransformation.apply("Hello", range("1", " 4 ")));
    }

    // An endIndex of -1 means the end of the text
    @Test
    void negativeEndMeansEndOfText() {
        assertEquals("llo", substringTransformation.apply("Hello", range(2, -1)));
    }

    // Uses the end of the text when endIndex is missing
    @Test
    void missingEndMeansEndOfText() {
        Map<String, Object> params = new HashMap<>();
        params.put("startIndex", 2);

        assertEquals("llo", substringTransformation.apply("Hello", params));
    }

    // Returns the whole text when no parameters are given
    @Test
    void noParametersReturnsWholeText() {
        assertEquals("Hello", substringTransformation.apply("Hello", null));
        assertEquals("Hello", substringTransformation.apply("Hello", new HashMap<>()));
    }

    // Clamps an endIndex past the end of the text to the text length
    @Test
    void clampsEndIndex() {
        assertEquals("llo", substringTransformation.apply("Hello", range(2, 100)));
    }

    // Treats a negative startIndex as 0
    @Test
    void clampsNegativeStartIndex() {
        assertEquals("Hel", substringTransformation.apply("Hello", range(-5, 3)));
    }

    // Returns an empty string when startIndex is at or past the end of the text
    @Test
    void startIndexPastEndReturnsEmpty() {
        assertEquals("", substringTransformation.apply("Hello", range(5, -1)));
        assertEquals("", substringTransformation.apply("Hello", range(10, -1)));
    }

    // Returns an empty string when startIndex is not before endIndex
    @Test
    void startNotBeforeEndReturnsEmpty() {
        assertEquals("", substringTransformation.apply("Hello", range(3, 3)));
        assertEquals("", substringTransformation.apply("Hello", range(4, 2)));
    }

    // Uses the default index when a value is not a valid integer
    @Test
    void invalidIndexesUseDefaults() {
        assertEquals("Hel", substringTransformation.apply("Hello", range("abc", 3)));
        assertEquals("llo", substringTransformation.apply("Hello", range(2, "xyz")));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", substringTransformation.apply(null, range(0, 2)));
    }

    // Returns an empty string for empty input
    @Test
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", substringTransformation.apply("", range(0, 2)));
    }
}
