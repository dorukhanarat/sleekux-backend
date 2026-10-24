package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SplitJoinTransformationTest {

    private SplitJoinTransformation splitJoinTransformation;

    @BeforeEach
    void setUp() {
        splitJoinTransformation = new SplitJoinTransformation();
    }

    private Map<String, Object> params(Object splitBy, Object joinWith, Object trimItems) {
        Map<String, Object> params = new HashMap<>();
        params.put("splitBy", splitBy);
        params.put("joinWith", joinWith);
        params.put("trimItems", trimItems);
        return params;
    }

    // Has key "split_join" and display name "Split & Join"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("split_join", splitJoinTransformation.getKey());
        assertEquals("Split & Join", splitJoinTransformation.getDisplayName());
    }

    // Defines optional splitBy (","), joinWith ("\n") and trimItems (true) parameters
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = splitJoinTransformation.getParameters();

        assertEquals(3, parameters.size());
        assertEquals("splitBy", parameters.get(0).getName());
        assertEquals(",", parameters.get(0).getDefaultValue());
        assertEquals("joinWith", parameters.get(1).getName());
        assertEquals("\n", parameters.get(1).getDefaultValue());
        assertEquals("trimItems", parameters.get(2).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(2).getType());
        assertEquals(true, parameters.get(2).getDefaultValue());
        parameters.forEach(p -> assertFalse(p.isRequired()));
    }

    // Turns a comma-separated list into trimmed lines by default
    @Test
    void splitsCommasIntoLinesByDefault() {
        assertEquals("a\nb\nc", splitJoinTransformation.apply("a, b ,c", new HashMap<>()));
        assertEquals("a\nb\nc", splitJoinTransformation.apply("a, b ,c", null));
    }

    // Uses custom separators
    @Test
    void usesCustomSeparators() {
        assertEquals("a, b, c", splitJoinTransformation.apply("a|b|c", params("|", ", ", null)));
        assertEquals("a-b-c", splitJoinTransformation.apply("a::b::c", params("::", "-", null)));
    }

    // Lines can be joined back into one line
    @Test
    void joinsLines() {
        assertEquals("a,b", splitJoinTransformation.apply("a\nb", params("\n", ",", null)));
    }

    // splitBy is matched literally, not as a regex
    @Test
    void splitByIsLiteral() {
        assertEquals("a/b/c", splitJoinTransformation.apply("a.b.c", params(".", "/", null)));
    }

    // trimItems false keeps the whitespace around items
    @Test
    void keepsWhitespaceWhenTrimItemsIsFalse() {
        assertEquals("a; b", splitJoinTransformation.apply("a, b", params(",", ";", false)));
    }

    // Empty items are kept
    @Test
    void keepsEmptyItems() {
        assertEquals("a||b|", splitJoinTransformation.apply("a,,b,", params(",", "|", null)));
    }

    // Text without the separator is returned as a single (trimmed) item
    @Test
    void returnsSingleItemWithoutSeparator() {
        assertEquals("abc", splitJoinTransformation.apply(" abc ", params(",", "|", null)));
    }

    // Returns the text unchanged when splitBy is empty
    @Test
    void returnsInputForEmptySplitBy() {
        assertEquals(" a, b ", splitJoinTransformation.apply(" a, b ", params("", "|", null)));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", splitJoinTransformation.apply(null, null));
    }
}
