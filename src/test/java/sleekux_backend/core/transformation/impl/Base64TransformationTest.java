package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Base64TransformationTest {

    private Base64Transformation base64Transformation;

    @BeforeEach
    void setUp() {
        base64Transformation = new Base64Transformation();
    }

    private Map<String, Object> params(Object mode, Object urlSafe) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", mode);
        params.put("urlSafe", urlSafe);
        return params;
    }

    // Has key "base64" and display name "Base64 Encode/Decode"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("base64", base64Transformation.getKey());
        assertEquals("Base64 Encode/Decode", base64Transformation.getDisplayName());
    }

    // Defines an optional mode (default "encode") and an optional urlSafe flag (default false)
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = base64Transformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("encode", parameters.get(0).getDefaultValue());
        assertEquals("urlSafe", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertEquals(false, parameters.get(1).getDefaultValue());
    }

    // Encodes text to standard Base64 with padding
    @Test
    void encodes() {
        assertEquals("SGVsbG8sIFdvcmxkIQ==", base64Transformation.apply("Hello, World!", params("encode", false)));
    }

    // Encodes non-ASCII text as UTF-8
    @Test
    void encodesUtf8() {
        assertEquals("w4dhxJ9yxLE=", base64Transformation.apply("Çağrı", params("encode", false)));
    }

    // Decodes Base64 back to the original text
    @Test
    void decodes() {
        assertEquals("Hello, World!", base64Transformation.apply("SGVsbG8sIFdvcmxkIQ==", params("decode", false)));
        assertEquals("Çağrı", base64Transformation.apply("w4dhxJ9yxLE=", params("decode", false)));
    }

    // Encoding and then decoding gives back the original text
    @Test
    void roundTrips() {
        String text = "Line 1\nLine 2 – with emoji 😀 and symbols <>&";

        String encoded = base64Transformation.apply(text, params("encode", false));

        assertEquals(text, base64Transformation.apply(encoded, params("decode", false)));
    }

    // Decoding accepts input without padding
    @Test
    void decodesWithoutPadding() {
        assertEquals("Hello, World!", base64Transformation.apply("SGVsbG8sIFdvcmxkIQ", params("decode", false)));
    }

    // Decoding ignores spaces and line breaks in the input
    @Test
    void decodeIgnoresWhitespace() {
        assertEquals("Hello, World!", base64Transformation.apply(" SGVsbG8s\nIFdvcmxk\r\nIQ== ", params("decode", false)));
    }

    // The URL-safe alphabet uses - and _ instead of + and /
    @Test
    void urlSafeAlphabet() {
        String text = "???>>>";

        assertEquals("Pz8/Pj4+", base64Transformation.apply(text, params("encode", false)));
        assertEquals("Pz8_Pj4-", base64Transformation.apply(text, params("encode", true)));
        assertEquals(text, base64Transformation.apply("Pz8_Pj4-", params("decode", true)));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("Hi", base64Transformation.apply("SGk=", params("DECODE", false)));
    }

    // Encodes when the mode is missing, null, unknown or the map is null
    @Test
    void defaultsToEncode() {
        assertEquals("SGk=", base64Transformation.apply("Hi", new HashMap<>()));
        assertEquals("SGk=", base64Transformation.apply("Hi", params(null, null)));
        assertEquals("SGk=", base64Transformation.apply("Hi", params("compress", false)));
        assertEquals("SGk=", base64Transformation.apply("Hi", null));
    }

    // Decoding text that is not Base64 throws an exception
    @Test
    void invalidBase64Throws() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> base64Transformation.apply("not base64!", params("decode", false)));

        assertTrue(e.getMessage().startsWith("Invalid Base64 input"));
    }

    // Decoding URL-safe input with the standard alphabet throws an exception
    @Test
    void wrongAlphabetThrows() {
        assertThrows(IllegalArgumentException.class, () -> base64Transformation.apply("Pz8_Pj4-", params("decode", false)));
    }

    // Decoding bytes that are not valid UTF-8 text throws an exception
    @Test
    void nonUtf8BytesThrow() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> base64Transformation.apply("/w==", params("decode", false)));

        assertEquals("Decoded Base64 data is not valid UTF-8 text", e.getMessage());
    }

    // Returns an empty string for null or empty input in both modes
    @Test
    void returnsEmptyStringForNullOrEmptyInput() {
        assertEquals("", base64Transformation.apply(null, params("encode", false)));
        assertEquals("", base64Transformation.apply("", params("encode", false)));
        assertEquals("", base64Transformation.apply("", params("decode", false)));
    }
}
