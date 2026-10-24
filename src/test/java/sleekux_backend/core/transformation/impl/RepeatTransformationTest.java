package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RepeatTransformationTest {

    private RepeatTransformation repeatTransformation;

    @BeforeEach
    void setUp() {
        repeatTransformation = new RepeatTransformation();
    }

    private Map<String, Object> params(Object count, Object separator) {
        Map<String, Object> params = new HashMap<>();
        params.put("count", count);
        params.put("separator", separator);
        return params;
    }

    // Has key "repeat" and display name "Repeat Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("repeat", repeatTransformation.getKey());
        assertEquals("Repeat Text", repeatTransformation.getDisplayName());
    }

    // Defines an optional count (default 2) and an optional separator (default "")
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = repeatTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("count", parameters.get(0).getName());
        assertEquals(ParameterType.NUMBER, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals(2, parameters.get(0).getDefaultValue());
        assertEquals("separator", parameters.get(1).getName());
        assertEquals("", parameters.get(1).getDefaultValue());
    }

    // Repeats twice without a separator by default
    @Test
    void repeatsTwiceByDefault() {
        assertEquals("abab", repeatTransformation.apply("ab", new HashMap<>()));
        assertEquals("abab", repeatTransformation.apply("ab", null));
    }

    // Places the separator between repetitions only
    @Test
    void usesSeparator() {
        assertEquals("ab, ab, ab", repeatTransformation.apply("ab", params(3, ", ")));
    }

    // A count of 1 returns the text, 0 or less returns an empty string
    @Test
    void handlesSmallCounts() {
        assertEquals("ab", repeatTransformation.apply("ab", params(1, "-")));
        assertEquals("", repeatTransformation.apply("ab", params(0, "-")));
        assertEquals("", repeatTransformation.apply("ab", params(-3, "-")));
    }

    // Count can be sent as a string
    @Test
    void acceptsCountAsString() {
        assertEquals("aaa", repeatTransformation.apply("a", params("3", null)));
    }

    // Count is capped to avoid huge results
    @Test
    void capsCount() {
        assertEquals(RepeatTransformation.MAX_COUNT, repeatTransformation.apply("a", params(5000, "")).length());
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", repeatTransformation.apply(null, params(3, "")));
    }
}
