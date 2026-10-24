package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WrapLinesTransformationTest {

    private WrapLinesTransformation wrapLinesTransformation;

    @BeforeEach
    void setUp() {
        wrapLinesTransformation = new WrapLinesTransformation();
    }

    private Map<String, Object> params(Object prefix, Object suffix) {
        Map<String, Object> params = new HashMap<>();
        params.put("prefix", prefix);
        params.put("suffix", suffix);
        return params;
    }

    // Has key "wrap_lines" and display name "Wrap Each Line"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("wrap_lines", wrapLinesTransformation.getKey());
        assertEquals("Wrap Each Line", wrapLinesTransformation.getDisplayName());
    }

    // Defines optional prefix and suffix parameters that default to ""
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = wrapLinesTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("prefix", parameters.get(0).getName());
        assertEquals("suffix", parameters.get(1).getName());
        parameters.forEach(p -> {
            assertEquals(ParameterType.TEXT, p.getType());
            assertFalse(p.isRequired());
            assertEquals("", p.getDefaultValue());
        });
    }

    // Adds the prefix and suffix to every line
    @Test
    void wrapsEveryLine() {
        assertEquals("'a',\n'b',", wrapLinesTransformation.apply("a\nb", params("'", "',")));
    }

    // Works with only a prefix or only a suffix
    @Test
    void wrapsWithPrefixOrSuffixOnly() {
        assertEquals("- a\n- b", wrapLinesTransformation.apply("a\nb", params("- ", null)));
        assertEquals("a;\nb;", wrapLinesTransformation.apply("a\nb", params(null, ";")));
    }

    // Returns the lines unchanged without prefix and suffix
    @Test
    void returnsInputWithoutParameters() {
        assertEquals("a\nb", wrapLinesTransformation.apply("a\nb", new HashMap<>()));
        assertEquals("a\nb", wrapLinesTransformation.apply("a\nb", null));
    }

    // Handles CRLF line endings
    @Test
    void handlesCrlf() {
        assertEquals(">a\n>b", wrapLinesTransformation.apply("a\r\nb", params(">", null)));
    }

    // Returns an empty string for empty or null input
    @Test
    void returnsEmptyStringForEmptyOrNullInput() {
        assertEquals("", wrapLinesTransformation.apply("", params("[", "]")));
        assertEquals("", wrapLinesTransformation.apply(null, params("[", "]")));
    }
}
