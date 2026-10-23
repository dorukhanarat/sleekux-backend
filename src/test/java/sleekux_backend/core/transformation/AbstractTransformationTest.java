package sleekux_backend.core.transformation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractTransformationTest {

    private static class TestTransformation extends AbstractTransformation {
        TestTransformation(List<ParameterDefinition> parameters) {
            super("test", "Test Transformation", parameters);
        }

        @Override
        public String apply(String input, Map<String, Object> parameters) {
            return input;
        }
    }

    private TestTransformation transformation;

    @BeforeEach
    void setUp() {
        transformation = new TestTransformation(List.of());
    }

    private Map<String, Object> param(String name, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(name, value);
        return params;
    }

    // Getters return the key, display name and parameters passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        List<ParameterDefinition> parameters = List.of(
                new ParameterDefinition("count", "Count", ParameterType.NUMBER, true, 1, ""));
        TestTransformation withParams = new TestTransformation(parameters);

        assertEquals("test", withParams.getKey());
        assertEquals("Test Transformation", withParams.getDisplayName());
        assertEquals(parameters, withParams.getParameters());
    }

    // getStringParam returns the parameter value
    @Test
    void stringParamReturnsValue() {
        assertEquals("hello", transformation.getStringParam(param("text", "hello"), "text", "default"));
    }

    // getStringParam converts non-string values to text
    @Test
    void stringParamConvertsNonStringValues() {
        assertEquals("42", transformation.getStringParam(param("text", 42), "text", "default"));
        assertEquals("true", transformation.getStringParam(param("text", true), "text", "default"));
    }

    // getStringParam returns the default when the value is missing, null or the map is null
    @Test
    void stringParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertEquals("default", transformation.getStringParam(new HashMap<>(), "text", "default"));
        assertEquals("default", transformation.getStringParam(param("text", null), "text", "default"));
        assertEquals("default", transformation.getStringParam(null, "text", "default"));
    }

    // getStringParam keeps an empty string instead of using the default
    @Test
    void stringParamKeepsEmptyString() {
        assertEquals("", transformation.getStringParam(param("text", ""), "text", "default"));
    }

    // getIntParam parses integers and numeric strings, including negatives
    @Test
    void intParamParsesNumbers() {
        assertEquals(42, transformation.getIntParam(param("count", 42), "count", 0));
        assertEquals(42, transformation.getIntParam(param("count", "42"), "count", 0));
        assertEquals(-7, transformation.getIntParam(param("count", "-7"), "count", 0));
    }

    // getIntParam ignores surrounding whitespace
    @Test
    void intParamTrimsWhitespace() {
        assertEquals(42, transformation.getIntParam(param("count", "  42 "), "count", 0));
    }

    // getIntParam returns the default when the value is missing, null or the map is null
    @Test
    void intParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertEquals(5, transformation.getIntParam(new HashMap<>(), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", null), "count", 5));
        assertEquals(5, transformation.getIntParam(null, "count", 5));
    }

    // getIntParam returns the default for text, empty strings and decimals
    @Test
    void intParamReturnsDefaultWhenNotAnInteger() {
        assertEquals(5, transformation.getIntParam(param("count", "abc"), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", ""), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", "3.5"), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", 3.5), "count", 5));
    }

    // getIntParam returns the default for numbers too large for an int
    @Test
    void intParamReturnsDefaultOnOverflow() {
        assertEquals(5, transformation.getIntParam(param("count", "99999999999"), "count", 5));
    }

    // getBooleanParam parses booleans and "true"/"false" strings
    @Test
    void booleanParamParsesValues() {
        assertTrue(transformation.getBooleanParam(param("flag", true), "flag", false));
        assertTrue(transformation.getBooleanParam(param("flag", "true"), "flag", false));
        assertFalse(transformation.getBooleanParam(param("flag", "false"), "flag", true));
    }

    // getBooleanParam ignores case and surrounding whitespace
    @Test
    void booleanParamIsCaseInsensitiveAndTrimmed() {
        assertTrue(transformation.getBooleanParam(param("flag", " TRUE "), "flag", false));
    }

    // getBooleanParam returns the default when the value is missing, null or the map is null
    @Test
    void booleanParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertTrue(transformation.getBooleanParam(new HashMap<>(), "flag", true));
        assertTrue(transformation.getBooleanParam(param("flag", null), "flag", true));
        assertTrue(transformation.getBooleanParam(null, "flag", true));
    }

    // getBooleanParam returns the default for unrecognised values
    @Test
    void booleanParamReturnsDefaultForUnrecognisedValues() {
        assertTrue(transformation.getBooleanParam(param("flag", "yes"), "flag", true));
        assertTrue(transformation.getBooleanParam(param("flag", "1"), "flag", true));
        assertTrue(transformation.getBooleanParam(param("flag", ""), "flag", true));
        assertFalse(transformation.getBooleanParam(param("flag", "yes"), "flag", false));
    }
}
