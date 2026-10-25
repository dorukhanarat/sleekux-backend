package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlEscapeTransformationTest {

    private HtmlEscapeTransformation htmlEscapeTransformation;

    @BeforeEach
    void setUp() {
        htmlEscapeTransformation = new HtmlEscapeTransformation();
    }

    private Map<String, Object> mode(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", value);
        return params;
    }

    // Has key "html_escape" and display name "Escape HTML"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("html_escape", htmlEscapeTransformation.getKey());
        assertEquals("Escape HTML", htmlEscapeTransformation.getDisplayName());
    }

    // Defines a single optional mode parameter that defaults to "escape"
    @Test
    void definesModeParameter() {
        List<ParameterDefinition> parameters = htmlEscapeTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("escape", parameters.get(0).getDefaultValue());
    }

    // Escapes the five special HTML characters
    @Test
    void escapesSpecialCharacters() {
        assertEquals("&lt;a href=&quot;x&quot; title=&#39;y&#39;&gt;Tom &amp; Jerry&lt;/a&gt;",
                htmlEscapeTransformation.apply("<a href=\"x\" title='y'>Tom & Jerry</a>", mode("escape")));
    }

    // Escaping leaves other text, including non-ASCII characters, unchanged
    @Test
    void escapeKeepsOtherText() {
        assertEquals("Çay 😀 100%", htmlEscapeTransformation.apply("Çay 😀 100%", mode("escape")));
    }

    // Escaping already escaped text escapes the ampersand again
    @Test
    void escapeIsNotIdempotent() {
        assertEquals("&amp;lt;", htmlEscapeTransformation.apply("&lt;", mode("escape")));
    }

    // Unescapes the common named entities
    @Test
    void unescapesNamedEntities() {
        assertEquals("<b>\"Tom\" & 'Jerry'</b>",
                htmlEscapeTransformation.apply("&lt;b&gt;&quot;Tom&quot; &amp; &apos;Jerry&apos;&lt;/b&gt;", mode("unescape")));
    }

    // Unescapes a non-breaking space entity
    @Test
    void unescapesNbsp() {
        assertEquals("a" + (char) 0xA0 + "b", htmlEscapeTransformation.apply("a&nbsp;b", mode("unescape")));
    }

    // Unescapes decimal and hexadecimal numeric entities, including emoji
    @Test
    void unescapesNumericEntities() {
        assertEquals("'A'", htmlEscapeTransformation.apply("&#39;&#65;&#x27;", mode("unescape")));
        assertEquals("😀", htmlEscapeTransformation.apply("&#x1F600;", mode("unescape")));
        assertEquals("😀", htmlEscapeTransformation.apply("&#128512;", mode("unescape")));
    }

    // Unescapes in a single pass, so a double-escaped entity becomes single-escaped
    @Test
    void unescapesOnlyOnce() {
        assertEquals("&lt;", htmlEscapeTransformation.apply("&amp;lt;", mode("unescape")));
    }

    // Leaves unknown entities, invalid code points and lone ampersands unchanged
    @Test
    void keepsUnknownEntities() {
        assertEquals("&copy; &#99999999; &#xZZ; a & b", htmlEscapeTransformation.apply("&copy; &#99999999; &#xZZ; a & b", mode("unescape")));
    }

    // Escaping and then unescaping gives back the original text
    @Test
    void roundTrips() {
        String text = "<p class=\"x\">It's 5 > 3 & 2 < 4</p>";

        String escaped = htmlEscapeTransformation.apply(text, mode("escape"));

        assertEquals(text, htmlEscapeTransformation.apply(escaped, mode("unescape")));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("<", htmlEscapeTransformation.apply("&lt;", mode("UNESCAPE")));
    }

    // Escapes when the mode is missing, null, unknown or the map is null
    @Test
    void defaultsToEscape() {
        assertEquals("&lt;", htmlEscapeTransformation.apply("<", new HashMap<>()));
        assertEquals("&lt;", htmlEscapeTransformation.apply("<", mode(null)));
        assertEquals("&lt;", htmlEscapeTransformation.apply("<", mode("other")));
        assertEquals("&lt;", htmlEscapeTransformation.apply("<", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", htmlEscapeTransformation.apply(null, mode("escape")));
    }
}
