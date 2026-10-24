package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SortLinesTransformationTest {

    private SortLinesTransformation sortLinesTransformation;

    @BeforeEach
    void setUp() {
        sortLinesTransformation = new SortLinesTransformation();
    }

    private Map<String, Object> params(Object order, Object caseSensitive, Object numeric) {
        Map<String, Object> params = new HashMap<>();
        params.put("order", order);
        params.put("caseSensitive", caseSensitive);
        params.put("numeric", numeric);
        return params;
    }

    // Has key "sort_lines" and display name "Sort Lines"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("sort_lines", sortLinesTransformation.getKey());
        assertEquals("Sort Lines", sortLinesTransformation.getDisplayName());
    }

    // Defines optional order, caseSensitive and numeric parameters
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = sortLinesTransformation.getParameters();

        assertEquals(3, parameters.size());
        assertEquals("order", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertEquals("asc", parameters.get(0).getDefaultValue());
        assertEquals("caseSensitive", parameters.get(1).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(1).getType());
        assertEquals(true, parameters.get(1).getDefaultValue());
        assertEquals("numeric", parameters.get(2).getName());
        assertEquals(ParameterType.BOOLEAN, parameters.get(2).getType());
        assertEquals(false, parameters.get(2).getDefaultValue());
        parameters.forEach(p -> assertFalse(p.isRequired()));
    }

    // Sorts lines in ascending order by default
    @Test
    void sortsAscendingByDefault() {
        assertEquals("apple\nbanana\ncherry", sortLinesTransformation.apply("banana\napple\ncherry", new HashMap<>()));
        assertEquals("apple\nbanana\ncherry", sortLinesTransformation.apply("banana\napple\ncherry", null));
    }

    // Order "desc" sorts in descending order, and the order is case-insensitive
    @Test
    void sortsDescending() {
        assertEquals("cherry\nbanana\napple", sortLinesTransformation.apply("banana\napple\ncherry", params("desc", null, null)));
        assertEquals("cherry\nbanana\napple", sortLinesTransformation.apply("banana\napple\ncherry", params("DESC", null, null)));
    }

    // An unknown order sorts ascending
    @Test
    void unknownOrderSortsAscending() {
        assertEquals("a\nb", sortLinesTransformation.apply("b\na", params("random", null, null)));
    }

    // Case-sensitive sorting puts uppercase before lowercase
    @Test
    void sortsCaseSensitive() {
        assertEquals("A\nB\na\nb", sortLinesTransformation.apply("b\nB\na\nA", params("asc", true, null)));
    }

    // Case-insensitive sorting groups letters and keeps the original order of equal lines
    @Test
    void sortsCaseInsensitive() {
        assertEquals("a\nA\nb\nB", sortLinesTransformation.apply("b\nB\na\nA", params("asc", false, null)));
    }

    // Without numeric, numbers are sorted as text
    @Test
    void sortsNumbersAsTextByDefault() {
        assertEquals("10\n100\n9", sortLinesTransformation.apply("10\n9\n100", params("asc", null, false)));
    }

    // Numeric sorting compares by value, including decimals and negatives
    @Test
    void sortsNumerically() {
        assertEquals("9\n10\n100", sortLinesTransformation.apply("10\n9\n100", params("asc", null, true)));
        assertEquals("-1\n2.5\n 10", sortLinesTransformation.apply("2.5\n 10\n-1", params("asc", null, "true")));
    }

    // Numeric sorting puts non-number lines after the numbers in both orders
    @Test
    void sortsNonNumbersAfterNumbers() {
        assertEquals("2\n10\na\nb", sortLinesTransformation.apply("b\n10\na\n2", params("asc", null, true)));
        assertEquals("10\n2\nb\na", sortLinesTransformation.apply("b\n10\na\n2", params("desc", null, true)));
    }

    // Handles CRLF line endings
    @Test
    void handlesCrlf() {
        assertEquals("a\nb", sortLinesTransformation.apply("b\r\na", null));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", sortLinesTransformation.apply(null, null));
    }
}
