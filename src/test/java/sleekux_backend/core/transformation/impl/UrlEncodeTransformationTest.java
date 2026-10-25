package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UrlEncodeTransformationTest {

    private UrlEncodeTransformation urlEncodeTransformation;

    @BeforeEach
    void setUp() {
        urlEncodeTransformation = new UrlEncodeTransformation();
    }

    private Map<String, Object> mode(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", value);
        return params;
    }

    // Has key "url_encode" and display name "URL Encode/Decode"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("url_encode", urlEncodeTransformation.getKey());
        assertEquals("URL Encode/Decode", urlEncodeTransformation.getDisplayName());
    }

    // Defines a single optional mode parameter that defaults to "encode"
    @Test
    void definesModeParameter() {
        List<ParameterDefinition> parameters = urlEncodeTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("encode", parameters.get(0).getDefaultValue());
    }

    // Encodes spaces as %20 and reserved characters as percent codes
    @Test
    void encodesReservedCharacters() {
        assertEquals("a%20b%26c%3Dd%2Fe%3Ff%23g", urlEncodeTransformation.apply("a b&c=d/e?f#g", mode("encode")));
    }

    // Encodes a plus sign so it is not read as a space
    @Test
    void encodesPlusSign() {
        assertEquals("1%2B1", urlEncodeTransformation.apply("1+1", mode("encode")));
    }

    // Leaves unreserved characters (letters, digits, - _ . ~) unencoded
    @Test
    void keepsUnreservedCharacters() {
        assertEquals("AZaz09-_.~", urlEncodeTransformation.apply("AZaz09-_.~", mode("encode")));
    }

    // Encodes the asterisk, which URLEncoder normally leaves as it is
    @Test
    void encodesAsterisk() {
        assertEquals("a%2Ab", urlEncodeTransformation.apply("a*b", mode("encode")));
    }

    // Encodes non-ASCII characters as UTF-8 bytes
    @Test
    void encodesUtf8() {
        assertEquals("%C3%A7ay", urlEncodeTransformation.apply("çay", mode("encode")));
    }

    // Decodes percent codes back to the original text
    @Test
    void decodes() {
        assertEquals("a b&c=d", urlEncodeTransformation.apply("a%20b%26c%3Dd", mode("decode")));
        assertEquals("çay", urlEncodeTransformation.apply("%C3%A7ay", mode("decode")));
    }

    // Decoding also treats + as a space, as in HTML form data
    @Test
    void decodesPlusAsSpace() {
        assertEquals("a b", urlEncodeTransformation.apply("a+b", mode("decode")));
    }

    // Encoding and then decoding gives back the original text
    @Test
    void roundTrips() {
        String text = "https://example.com/search?q=çay & simit*~+";

        String encoded = urlEncodeTransformation.apply(text, mode("encode"));

        assertEquals(text, urlEncodeTransformation.apply(encoded, mode("decode")));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("a b", urlEncodeTransformation.apply("a%20b", mode("Decode")));
    }

    // Encodes when the mode is missing, null, unknown or the map is null
    @Test
    void defaultsToEncode() {
        assertEquals("a%20b", urlEncodeTransformation.apply("a b", new HashMap<>()));
        assertEquals("a%20b", urlEncodeTransformation.apply("a b", mode(null)));
        assertEquals("a%20b", urlEncodeTransformation.apply("a b", mode("other")));
        assertEquals("a%20b", urlEncodeTransformation.apply("a b", null));
    }

    // Decoding an invalid percent code throws an exception
    @Test
    void invalidPercentEncodingThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> urlEncodeTransformation.apply("100%zz", mode("decode")));

        assertTrue(e.getMessage().startsWith("Invalid percent-encoding"));
        assertThrows(IllegalArgumentException.class, () -> urlEncodeTransformation.apply("100%", mode("decode")));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", urlEncodeTransformation.apply(null, mode("encode")));
    }
}
