package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PrependTransformationTest {

    private PrependTransformation prependTransformation;

    @BeforeEach
    void setUp() {
        prependTransformation = new PrependTransformation();
    }

    private Map<String, Object> prefix(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("prefix", value);
        return params;
    }

    // Has key "prepend" and display name "Prepend Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("prepend", prependTransformation.getKey());
        assertEquals("Prepend Text", prependTransformation.getDisplayName());
    }

    // Defines a single required prefix text parameter
    @Test
    void definesPrefixParameter() {
        List<ParameterDefinition> parameters = prependTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("prefix", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
    }

    // Prepends the prefix to the start of the input
    @Test
    void prependsPrefix() {
        assertEquals("Hello World", prependTransformation.apply("World", prefix("Hello ")));
    }

    // Returns just the prefix for null input
    @Test
    void treatsNullInputAsEmpty() {
        assertEquals("Hello", prependTransformation.apply(null, prefix("Hello")));
    }

    // Returns just the prefix for empty input
    @Test
    void prependsToEmptyInput() {
        assertEquals("Hello", prependTransformation.apply("", prefix("Hello")));
    }

    // Returns the input unchanged when the prefix is missing, null or the map is null
    @Test
    void returnsInputWhenPrefixMissing() {
        assertEquals("World", prependTransformation.apply("World", new HashMap<>()));
        assertEquals("World", prependTransformation.apply("World", prefix(null)));
        assertEquals("World", prependTransformation.apply("World", null));
    }

    // Converts a non-string prefix to text
    @Test
    void convertsNonStringPrefix() {
        assertEquals("1. Item", prependTransformation.apply(". Item", prefix(1)));
    }

    // Returns an empty string when both input and prefix are null
    @Test
    void returnsEmptyWhenEverythingNull() {
        assertEquals("", prependTransformation.apply(null, null));
    }
}
