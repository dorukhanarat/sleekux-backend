package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DedupeLinesTransformationTest {

    private DedupeLinesTransformation dedupeLinesTransformation;

    @BeforeEach
    void setUp() {
        dedupeLinesTransformation = new DedupeLinesTransformation();
    }

    private Map<String, Object> params(Object caseSensitive, Object trimLines) {
        Map<String, Object> params = new HashMap<>();
        params.put("caseSensitive", caseSensitive);
        params.put("trimLines", trimLines);
        return params;
    }

    // Has key "dedupe_lines" and display name "Remove Duplicate Lines"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("dedupe_lines", dedupeLinesTransformation.getKey());
        assertEquals("Remove Duplicate Lines", dedupeLinesTransformation.getDisplayName());
    }

    // Defines optional caseSensitive (default true) and trimLines (default false) flags
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = dedupeLinesTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("caseSensitive", parameters.get(0).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(0).getType());
        assertEquals(true, parameters.get(0).getDefaultValue());
        assertEquals("trimLines", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertEquals(false, parameters.get(1).getDefaultValue());
        parameters.forEach(p -> assertFalse(p.isRequired()));
    }

    // Removes duplicates and keeps the first occurrence in order
    @Test
    void removesDuplicatesKeepingOrder() {
        assertEquals("a\nb\nc", dedupeLinesTransformation.apply("a\nb\na\nc\nb", null));
    }

    // Comparison is case-sensitive by default
    @Test
    void isCaseSensitiveByDefault() {
        assertEquals("Apple\napple", dedupeLinesTransformation.apply("Apple\napple", new HashMap<>()));
    }

    // caseSensitive false treats different cases as duplicates
    @Test
    void ignoresCaseWhenRequested() {
        assertEquals("Apple", dedupeLinesTransformation.apply("Apple\napple\nAPPLE", params(false, null)));
        assertEquals("Apple", dedupeLinesTransformation.apply("Apple\napple", params("false", null)));
    }

    // Surrounding whitespace counts by default and is ignored when trimLines is true
    @Test
    void trimLinesIgnoresSurroundingWhitespace() {
        assertEquals("a\n a \nb", dedupeLinesTransformation.apply("a\n a \nb", params(null, false)));
        assertEquals("a\nb", dedupeLinesTransformation.apply("a\n a \nb", params(null, true)));
    }

    // The kept line is returned in its original form
    @Test
    void keepsOriginalFormOfFirstLine() {
        assertEquals("  Foo", dedupeLinesTransformation.apply("  Foo\nfoo", params(false, true)));
    }

    // Handles CRLF line endings
    @Test
    void handlesCrlf() {
        assertEquals("a", dedupeLinesTransformation.apply("a\r\na", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", dedupeLinesTransformation.apply(null, null));
    }
}
