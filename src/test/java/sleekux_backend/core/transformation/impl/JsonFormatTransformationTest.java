package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonFormatTransformationTest {

    private JsonFormatTransformation jsonFormatTransformation;

    @BeforeEach
    void setUp() {
        jsonFormatTransformation = new JsonFormatTransformation();
    }

    private Map<String, Object> params(Object mode, Object indent) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", mode);
        params.put("indent", indent);
        return params;
    }

    // Has key "json_format" and display name "Format JSON"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("json_format", jsonFormatTransformation.getKey());
        assertEquals("Format JSON", jsonFormatTransformation.getDisplayName());
    }

    // Defines an optional mode (default "pretty") and an optional indent (default 2)
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = jsonFormatTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertEquals("pretty", parameters.get(0).getDefaultValue());
        assertEquals("indent", parameters.get(1).getName());
        assertEquals(ParameterType.NUMBER, parameters.get(1).getType());
        assertEquals(2, parameters.get(1).getDefaultValue());
    }

    // Pretty prints with one value per line and two-space indentation
    @Test
    void prettyPrints() {
        String expected = """
                {
                  "name": "Ali",
                  "tags": [
                    "a",
                    "b"
                  ],
                  "address": {
                    "city": "Ankara"
                  }
                }""";

        assertEquals(expected, jsonFormatTransformation.apply("{\"name\":\"Ali\",\"tags\":[\"a\",\"b\"],\"address\":{\"city\":\"Ankara\"}}", params("pretty", 2)));
    }

    // Uses the given number of spaces per level
    @Test
    void usesCustomIndent() {
        assertEquals("{\n    \"a\": [\n        1\n    ]\n}", jsonFormatTransformation.apply("{\"a\":[1]}", params("pretty", 4)));
    }

    // Limits the indent to between 1 and 8 spaces
    @Test
    void clampsIndent() {
        assertEquals("{\n \"a\": 1\n}", jsonFormatTransformation.apply("{\"a\":1}", params("pretty", 0)));
        assertEquals("{\n        \"a\": 1\n}", jsonFormatTransformation.apply("{\"a\":1}", params("pretty", 50)));
    }

    // Writes empty objects and arrays compactly
    @Test
    void writesEmptyContainersCompactly() {
        assertEquals("{\n  \"o\": {},\n  \"a\": []\n}", jsonFormatTransformation.apply("{\"o\":{},\"a\":[]}", params("pretty", 2)));
    }

    // Minify removes all whitespace between tokens but keeps whitespace inside strings
    @Test
    void minifies() {
        String input = """
                {
                  "message" : "hello  world",
                  "values" : [ 1, 2, 3 ]
                }
                """;

        assertEquals("{\"message\":\"hello  world\",\"values\":[1,2,3]}", jsonFormatTransformation.apply(input, params("minify", null)));
    }

    // Keeps the order of object keys
    @Test
    void keepsKeyOrder() {
        assertEquals("{\"z\":1,\"a\":2,\"m\":3}", jsonFormatTransformation.apply("{\"z\":1,\"a\":2,\"m\":3}", params("minify", null)));
    }

    // Keeps the exact value of large and precise numbers
    @Test
    void keepsNumberPrecision() {
        assertEquals("[12345678901234567890,0.1,1.50,1E+400]",
                jsonFormatTransformation.apply("[12345678901234567890, 0.1, 1.50, 1e400]", params("minify", null)));
    }

    // Keeps non-ASCII characters and escapes in strings
    @Test
    void keepsStringContent() {
        assertEquals("{\"text\":\"Çay \\\"sıcak\\\"\\n😀\"}",
                jsonFormatTransformation.apply("{ \"text\": \"Çay \\\"sıcak\\\"\\n😀\" }", params("minify", null)));
    }

    // Formats JSON whose top level is an array or a single value
    @Test
    void formatsNonObjectTopLevel() {
        assertEquals("[\n  1,\n  2\n]", jsonFormatTransformation.apply("[1,2]", params("pretty", 2)));
        assertEquals("\"text\"", jsonFormatTransformation.apply(" \"text\" ", params("pretty", 2)));
        assertEquals("null", jsonFormatTransformation.apply("null", params("minify", null)));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("{\"a\":1}", jsonFormatTransformation.apply("{ \"a\": 1 }", params("MINIFY", null)));
    }

    // Pretty prints when the mode is missing, null, unknown or the map is null
    @Test
    void defaultsToPretty() {
        String expected = "{\n  \"a\": 1\n}";

        assertEquals(expected, jsonFormatTransformation.apply("{\"a\":1}", new HashMap<>()));
        assertEquals(expected, jsonFormatTransformation.apply("{\"a\":1}", params(null, null)));
        assertEquals(expected, jsonFormatTransformation.apply("{\"a\":1}", params("other", null)));
        assertEquals(expected, jsonFormatTransformation.apply("{\"a\":1}", null));
    }

    // Invalid JSON throws an exception that includes the line and column
    @Test
    void invalidJsonThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> jsonFormatTransformation.apply("{\n  \"a\": }", params("pretty", 2)));

        assertTrue(e.getMessage().startsWith("Invalid JSON: "));
        assertTrue(e.getMessage().contains("(line 2, column"));
    }

    // Unfinished JSON throws an exception without internal source details
    @Test
    void unfinishedJsonThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> jsonFormatTransformation.apply("{\"a\":1", params("pretty", 2)));

        assertTrue(e.getMessage().startsWith("Invalid JSON: Unexpected end-of-input"));
        assertFalse(e.getMessage().contains("REDACTED"));
    }

    // Extra content after the JSON value throws an exception
    @Test
    void trailingContentThrows() {
        assertThrows(IllegalArgumentException.class, () -> jsonFormatTransformation.apply("{} {}", params("pretty", 2)));
        assertThrows(IllegalArgumentException.class, () -> jsonFormatTransformation.apply("[1,]", params("pretty", 2)));
    }

    // Returns an empty string for null, empty or whitespace-only input
    @Test
    void returnsEmptyStringForBlankInput() {
        assertEquals("", jsonFormatTransformation.apply(null, params("pretty", 2)));
        assertEquals("", jsonFormatTransformation.apply("", params("pretty", 2)));
        assertEquals("", jsonFormatTransformation.apply("   \n", params("minify", null)));
    }
}
