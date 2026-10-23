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

    @Test
    @DisplayName("Getters return the key, display name and parameters passed to the constructor")
    void gettersReturnConstructorValues() {
        List<ParameterDefinition> parameters = List.of(
                new ParameterDefinition("count", "Count", ParameterType.NUMBER, true, 1, ""));
        TestTransformation withParams = new TestTransformation(parameters);

        assertEquals("test", withParams.getKey());
        assertEquals("Test Transformation", withParams.getDisplayName());
        assertEquals(parameters, withParams.getParameters());
    }

    @Test
    @DisplayName("getStringParam returns the parameter value")
    void stringParamReturnsValue() {
        assertEquals("hello", transformation.getStringParam(param("text", "hello"), "text", "default"));
    }

    @Test
    @DisplayName("getStringParam converts non-string values to text")
    void stringParamConvertsNonStringValues() {
        assertEquals("42", transformation.getStringParam(param("text", 42), "text", "default"));
        assertEquals("true", transformation.getStringParam(param("text", true), "text", "default"));
    }

    @Test
    @DisplayName("getStringParam returns the default when the value is missing, null or the map is null")
    void stringParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertEquals("default", transformation.getStringParam(new HashMap<>(), "text", "default"));
        assertEquals("default", transformation.getStringParam(param("text", null), "text", "default"));
        assertEquals("default", transformation.getStringParam(null, "text", "default"));
    }

    @Test
    @DisplayName("getStringParam keeps an empty string instead of using the default")
    void stringParamKeepsEmptyString() {
        assertEquals("", transformation.getStringParam(param("text", ""), "text", "default"));
    }

    @Test
    @DisplayName("getIntParam parses integers and numeric strings, including negatives")
    void intParamParsesNumbers() {
        assertEquals(42, transformation.getIntParam(param("count", 42), "count", 0));
        assertEquals(42, transformation.getIntParam(param("count", "42"), "count", 0));
        assertEquals(-7, transformation.getIntParam(param("count", "-7"), "count", 0));
    }

    @Test
    @DisplayName("getIntParam ignores surrounding whitespace")
    void intParamTrimsWhitespace() {
        assertEquals(42, transformation.getIntParam(param("count", "  42 "), "count", 0));
    }

    @Test
    @DisplayName("getIntParam returns the default when the value is missing, null or the map is null")
    void intParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertEquals(5, transformation.getIntParam(new HashMap<>(), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", null), "count", 5));
        assertEquals(5, transformation.getIntParam(null, "count", 5));
    }

    @Test
    @DisplayName("getIntParam returns the default for text, empty strings and decimals")
    void intParamReturnsDefaultWhenNotAnInteger() {
        assertEquals(5, transformation.getIntParam(param("count", "abc"), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", ""), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", "3.5"), "count", 5));
        assertEquals(5, transformation.getIntParam(param("count", 3.5), "count", 5));
    }

    @Test
    @DisplayName("getIntParam returns the default for numbers too large for an int")
    void intParamReturnsDefaultOnOverflow() {
        assertEquals(5, transformation.getIntParam(param("count", "99999999999"), "count", 5));
    }

    @Test
    @DisplayName("getBooleanParam parses booleans and \"true\"/\"false\" strings")
    void booleanParamParsesValues() {
        assertTrue(transformation.getBooleanParam(param("flag", true), "flag", false));
        assertTrue(transformation.getBooleanParam(param("flag", "true"), "flag", false));
        assertFalse(transformation.getBooleanParam(param("flag", "false"), "flag", true));
    }

    @Test
    @DisplayName("getBooleanParam ignores case and surrounding whitespace")
    void booleanParamIsCaseInsensitiveAndTrimmed() {
        assertTrue(transformation.getBooleanParam(param("flag", " TRUE "), "flag", false));
    }

    @Test
    @DisplayName("getBooleanParam returns the default when the value is missing, null or the map is null")
    void booleanParamReturnsDefaultWhenMissingNullOrNoMap() {
        assertTrue(transformation.getBooleanParam(new HashMap<>(), "flag", true));
        assertTrue(transformation.getBooleanParam(param("flag", null), "flag", true));
        assertTrue(transformation.getBooleanParam(null, "flag", true));
    }

    @Test
    @DisplayName("getBooleanParam returns false, not the default, for unrecognised values")
    void booleanParamTreatsUnrecognisedValuesAsFalse() {
        assertFalse(transformation.getBooleanParam(param("flag", "yes"), "flag", true));
        assertFalse(transformation.getBooleanParam(param("flag", "1"), "flag", true));
        assertFalse(transformation.getBooleanParam(param("flag", ""), "flag", true));
    }
}
