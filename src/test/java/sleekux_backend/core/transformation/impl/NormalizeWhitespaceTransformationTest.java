package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NormalizeWhitespaceTransformationTest {

    private NormalizeWhitespaceTransformation normalizeWhitespaceTransformation;

    @BeforeEach
    void setUp() {
        normalizeWhitespaceTransformation = new NormalizeWhitespaceTransformation();
    }

    private Map<String, Object> mode(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", value);
        return params;
    }

    // Has key "normalize_whitespace" and display name "Normalize Whitespace"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("normalize_whitespace", normalizeWhitespaceTransformation.getKey());
        assertEquals("Normalize Whitespace", normalizeWhitespaceTransformation.getDisplayName());
    }

    // Defines an optional mode parameter that defaults to "collapse"
    @Test
    void definesModeParameter() {
        List<ParameterDefinition> parameters = normalizeWhitespaceTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("collapse", parameters.get(0).getDefaultValue());
    }

    // Mode "collapse" turns whitespace runs into one space and trims the ends
    @Test
    void collapsesWhitespace() {
        assertEquals("a b c", normalizeWhitespaceTransformation.apply("  a   b \t\n c  ", mode("collapse")));
    }

    // Mode "collapse" also handles Unicode whitespace
    @Test
    void collapsesUnicodeWhitespace() {
        assertEquals("a b", normalizeWhitespaceTransformation.apply("a  b", mode("collapse")));
    }

    // Mode "all" removes every whitespace character
    @Test
    void removesAllWhitespace() {
        assertEquals("abcd", normalizeWhitespaceTransformation.apply(" a b\tc\nd ", mode("all")));
    }

    // Mode "blank_lines" drops empty and whitespace-only lines
    @Test
    void removesBlankLines() {
        assertEquals("a\nb\nc", normalizeWhitespaceTransformation.apply("a\n\n  \nb\r\n\nc", mode("blank_lines")));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("ab", normalizeWhitespaceTransformation.apply("a b", mode("ALL")));
    }

    // Collapses when the mode is missing, unknown or the map is null
    @Test
    void defaultsToCollapse() {
        assertEquals("a b", normalizeWhitespaceTransformation.apply(" a  b ", new HashMap<>()));
        assertEquals("a b", normalizeWhitespaceTransformation.apply(" a  b ", mode("squash")));
        assertEquals("a b", normalizeWhitespaceTransformation.apply(" a  b ", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", normalizeWhitespaceTransformation.apply(null, mode("collapse")));
    }
}
