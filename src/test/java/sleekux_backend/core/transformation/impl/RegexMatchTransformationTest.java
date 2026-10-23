package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class RegexMatchTransformationTest {

    private RegexMatchTransformation regexMatchTransformation;

    @BeforeEach
    void setUp() {
        regexMatchTransformation = new RegexMatchTransformation();
    }

    private Map<String, Object> pattern(Object pattern) {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", pattern);
        return params;
    }

    private Map<String, Object> params(Object pattern, Object matchAll, Object delimiter) {
        Map<String, Object> params = pattern(pattern);
        params.put("matchAll", matchAll);
        params.put("delimiter", delimiter);
        return params;
    }

    // Has key "regex_match" and display name "Regex Match"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("regex_match", regexMatchTransformation.getKey());
        assertEquals("Regex Match", regexMatchTransformation.getDisplayName());
    }

    // Defines pattern, matchAll and delimiter parameters with the expected types and defaults
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = regexMatchTransformation.getParameters();

        assertEquals(3, parameters.size());

        ParameterDefinition pattern = parameters.get(0);
        assertEquals("pattern", pattern.getName());
        assertEquals(ParameterType.REGEX, pattern.getType());
        assertTrue(pattern.isRequired());

        ParameterDefinition matchAll = parameters.get(1);
        assertEquals("matchAll", matchAll.getName());
        assertEquals(ParameterType.BOOLEAN, matchAll.getType());
        assertFalse(matchAll.isRequired());
        assertEquals(true, matchAll.getDefaultValue());

        ParameterDefinition delimiter = parameters.get(2);
        assertEquals("delimiter", delimiter.getName());
        assertEquals(ParameterType.TEXT, delimiter.getType());
        assertFalse(delimiter.isRequired());
        assertEquals(", ", delimiter.getDefaultValue());
    }

    // Returns all matches joined with ", " by default
    @Test
    void returnsAllMatchesByDefault() {
        assertEquals("12, 345, 6", regexMatchTransformation.apply("a12b345c6", pattern("\\d+")));
    }

    // Joins matches with a custom delimiter
    @Test
    void usesCustomDelimiter() {
        assertEquals("12|345|6", regexMatchTransformation.apply("a12b345c6", params("\\d+", true, "|")));
    }

    // Joins matches with no separator when the delimiter is empty
    @Test
    void usesEmptyDelimiter() {
        assertEquals("123456", regexMatchTransformation.apply("a12b345c6", params("\\d+", true, "")));
    }

    // Uses ", " when the delimiter is null
    @Test
    void nullDelimiterUsesDefault() {
        assertEquals("12, 345, 6", regexMatchTransformation.apply("a12b345c6", params("\\d+", true, null)));
    }

    // Returns only the first match when matchAll is false
    @Test
    void returnsFirstMatchWhenMatchAllFalse() {
        assertEquals("12", regexMatchTransformation.apply("a12b345c6", params("\\d+", false, ", ")));
        assertEquals("12", regexMatchTransformation.apply("a12b345c6", params("\\d+", "false", ", ")));
    }

    // Returns all matches when matchAll is not a valid boolean
    @Test
    void invalidMatchAllUsesDefault() {
        assertEquals("12, 345, 6", regexMatchTransformation.apply("a12b345c6", params("\\d+", "yes", ", ")));
    }

    // Returns the whole match, not capture groups
    @Test
    void returnsWholeMatchNotGroups() {
        assertEquals("key=value", regexMatchTransformation.apply("key=value", pattern("(\\w+)=(\\w+)")));
    }

    // Returns a single match without a delimiter
    @Test
    void singleMatchHasNoDelimiter() {
        assertEquals("cat", regexMatchTransformation.apply("the cat sat", pattern("c\\w+")));
    }

    // Returns an empty string when nothing matches
    @Test
    void returnsEmptyWhenNoMatch() {
        assertEquals("", regexMatchTransformation.apply("abc", pattern("\\d+")));
        assertEquals("", regexMatchTransformation.apply("abc", params("\\d+", false, ", ")));
    }

    // Throws an exception when the pattern is invalid
    @Test
    void throwsForInvalidPattern() {
        assertThrows(PatternSyntaxException.class, () -> regexMatchTransformation.apply("abc(", pattern("(")));
    }

    // Returns the input unchanged when the pattern is missing, null, empty or the map is null
    @Test
    void returnsInputWhenPatternMissing() {
        assertEquals("abc", regexMatchTransformation.apply("abc", new HashMap<>()));
        assertEquals("abc", regexMatchTransformation.apply("abc", pattern(null)));
        assertEquals("abc", regexMatchTransformation.apply("abc", pattern("")));
        assertEquals("abc", regexMatchTransformation.apply("abc", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", regexMatchTransformation.apply(null, pattern("\\d+")));
    }

    // Returns an empty string for empty input
    @Test
    void returnsEmptyStringForEmptyInput() {
        assertEquals("", regexMatchTransformation.apply("", pattern("\\d+")));
    }
}
