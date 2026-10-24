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

public class FilterLinesTransformationTest {

    private FilterLinesTransformation filterLinesTransformation;

    @BeforeEach
    void setUp() {
        filterLinesTransformation = new FilterLinesTransformation();
    }

    private Map<String, Object> params(Object pattern, Object invert) {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", pattern);
        params.put("invert", invert);
        return params;
    }

    // Has key "filter_lines" and display name "Filter Lines"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("filter_lines", filterLinesTransformation.getKey());
        assertEquals("Filter Lines", filterLinesTransformation.getDisplayName());
    }

    // Defines a required pattern and an optional invert flag
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = filterLinesTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("pattern", parameters.get(0).getName());
        assertEquals(ParameterType.REGEX, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
        assertEquals("invert", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertFalse(parameters.get(1).isRequired());
        assertEquals(false, parameters.get(1).getDefaultValue());
    }

    // Keeps only the lines that match
    @Test
    void keepsMatchingLines() {
        assertEquals("apple\navocado", filterLinesTransformation.apply("apple\nbanana\navocado", params("^a", null)));
    }

    // Matches anywhere in the line, not only the whole line
    @Test
    void matchesPartOfLine() {
        assertEquals("error: disk\nnew error", filterLinesTransformation.apply("error: disk\nok\nnew error", params("error", null)));
    }

    // invert removes the matching lines, also when sent as a string
    @Test
    void invertRemovesMatchingLines() {
        assertEquals("banana", filterLinesTransformation.apply("apple\nbanana\navocado", params("^a", true)));
        assertEquals("banana", filterLinesTransformation.apply("apple\nbanana\navocado", params("^a", "true")));
    }

    // Returns an empty string when no line matches
    @Test
    void returnsEmptyWhenNothingMatches() {
        assertEquals("", filterLinesTransformation.apply("apple\nbanana", params("\\d", null)));
    }

    // Returns the text unchanged when the pattern is empty, missing or the map is null
    @Test
    void returnsInputWithoutPattern() {
        assertEquals("a\nb", filterLinesTransformation.apply("a\nb", params("", null)));
        assertEquals("a\nb", filterLinesTransformation.apply("a\nb", new HashMap<>()));
        assertEquals("a\nb", filterLinesTransformation.apply("a\nb", null));
    }

    // An invalid pattern throws PatternSyntaxException
    @Test
    void invalidPatternThrows() {
        assertThrows(PatternSyntaxException.class, () -> filterLinesTransformation.apply("abc", params("(abc", null)));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", filterLinesTransformation.apply(null, params("a", null)));
    }
}
