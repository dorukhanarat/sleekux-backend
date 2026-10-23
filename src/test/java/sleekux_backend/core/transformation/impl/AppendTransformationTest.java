package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AppendTransformationTest {

    private AppendTransformation appendTransformation;

    @BeforeEach
    void setUp() {
        appendTransformation = new AppendTransformation();
    }

    private Map<String, Object> suffix(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("suffix", value);
        return params;
    }

    // Has key "append" and display name "Append Text"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("append", appendTransformation.getKey());
        assertEquals("Append Text", appendTransformation.getDisplayName());
    }

    // Defines a single required suffix text parameter
    @Test
    void definesSuffixParameter() {
        List<ParameterDefinition> parameters = appendTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("suffix", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertTrue(parameters.get(0).isRequired());
    }

    // Appends the suffix to the end of the input
    @Test
    void appendsSuffix() {
        assertEquals("Hello World", appendTransformation.apply("Hello", suffix(" World")));
    }

    // Returns just the suffix for null input
    @Test
    void treatsNullInputAsEmpty() {
        assertEquals("World", appendTransformation.apply(null, suffix("World")));
    }

    // Returns just the suffix for empty input
    @Test
    void appendsToEmptyInput() {
        assertEquals("World", appendTransformation.apply("", suffix("World")));
    }

    // Returns the input unchanged when the suffix is missing, null or the map is null
    @Test
    void returnsInputWhenSuffixMissing() {
        assertEquals("Hello", appendTransformation.apply("Hello", new HashMap<>()));
        assertEquals("Hello", appendTransformation.apply("Hello", suffix(null)));
        assertEquals("Hello", appendTransformation.apply("Hello", null));
    }

    // Converts a non-string suffix to text
    @Test
    void convertsNonStringSuffix() {
        assertEquals("Version 2", appendTransformation.apply("Version ", suffix(2)));
    }

    // Returns an empty string when both input and suffix are null
    @Test
    void returnsEmptyWhenEverythingNull() {
        assertEquals("", appendTransformation.apply(null, null));
    }
}
