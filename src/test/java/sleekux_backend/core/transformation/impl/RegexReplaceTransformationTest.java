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

public class RegexReplaceTransformationTest {

    private RegexReplaceTransformation regexReplaceTransformation;

    @BeforeEach
    void setUp() {
        regexReplaceTransformation = new RegexReplaceTransformation();
    }

    private Map<String, Object> params(Object pattern, Object replacement, Object caseInsensitive) {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", pattern);
        params.put("replacement", replacement);
        params.put("caseInsensitive", caseInsensitive);
        return params;
    }

    // Has key "regex_replace" and display name "Regex Replace"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("regex_replace", regexReplaceTransformation.getKey());
        assertEquals("Regex Replace", regexReplaceTransformation.getDisplayName());
    }

    // Defines a required pattern, an optional replacement and an optional caseInsensitive flag
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = regexReplaceTransformation.getParameters();

        assertEquals(3, parameters.size());
        assertEquals("pattern", parameters.get(0).getName());
        assertEquals(ParameterType.REGEX, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
        assertEquals("replacement", parameters.get(1).getName());
        assertEquals(ParameterType.TEXT, parameters.get(1).getType());
        assertFalse(parameters.get(1).isRequired());
        assertEquals("", parameters.get(1).getDefaultValue());
        assertEquals("caseInsensitive", parameters.get(2).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(2).getType());
        assertEquals(false, parameters.get(2).getDefaultValue());
    }

    // Replaces every match of the pattern
    @Test
    void replacesAllMatches() {
        assertEquals("a#b#c#", regexReplaceTransformation.apply("a1b22c333", params("\\d+", "#", null)));
    }

    // Supports group references in the replacement
    @Test
    void supportsGroupReferences() {
        assertEquals("Smith, John", regexReplaceTransformation.apply("John Smith", params("(\\w+) (\\w+)", "$2, $1", null)));
    }

    // A missing replacement removes the matches
    @Test
    void missingReplacementRemovesMatches() {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", "\\d");

        assertEquals("ab", regexReplaceTransformation.apply("a1b2", params));
    }

    // Matching is case-sensitive by default
    @Test
    void isCaseSensitiveByDefault() {
        assertEquals("Hello hi HELLO", regexReplaceTransformation.apply("Hello hello HELLO", params("hello", "hi", null)));
    }

    // caseInsensitive ignores upper/lower case, also when sent as a string
    @Test
    void caseInsensitiveIgnoresCase() {
        assertEquals("hi hi hi", regexReplaceTransformation.apply("Hello hello HELLO", params("hello", "hi", true)));
        assertEquals("hi hi hi", regexReplaceTransformation.apply("Hello hello HELLO", params("hello", "hi", "true")));
    }

    // Returns the text unchanged when nothing matches
    @Test
    void returnsInputWhenNoMatch() {
        assertEquals("abc", regexReplaceTransformation.apply("abc", params("\\d", "#", null)));
    }

    // Returns the text unchanged when the pattern is empty, missing or the map is null
    @Test
    void returnsInputWithoutPattern() {
        assertEquals("abc", regexReplaceTransformation.apply("abc", params("", "#", null)));
        assertEquals("abc", regexReplaceTransformation.apply("abc", new HashMap<>()));
        assertEquals("abc", regexReplaceTransformation.apply("abc", null));
    }

    // An invalid pattern throws PatternSyntaxException
    @Test
    void invalidPatternThrows() {
        assertThrows(PatternSyntaxException.class, () -> regexReplaceTransformation.apply("abc", params("[abc", "#", null)));
    }

    // A reference to a group that does not exist throws IllegalArgumentException
    @Test
    void invalidGroupReferenceThrows() {
        assertThrows(IllegalArgumentException.class, () -> regexReplaceTransformation.apply("abc", params("(a)", "$5", null)));
        assertThrows(IllegalArgumentException.class, () -> regexReplaceTransformation.apply("abc", params("a", "$", null)));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", regexReplaceTransformation.apply(null, params("a", "b", null)));
    }
}
