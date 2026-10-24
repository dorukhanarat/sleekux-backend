package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NumberLinesTransformationTest {

    private NumberLinesTransformation numberLinesTransformation;

    @BeforeEach
    void setUp() {
        numberLinesTransformation = new NumberLinesTransformation();
    }

    private Map<String, Object> params(Object start, Object separator) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("separator", separator);
        return params;
    }

    // Has key "number_lines" and display name "Number Lines"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("number_lines", numberLinesTransformation.getKey());
        assertEquals("Number Lines", numberLinesTransformation.getDisplayName());
    }

    // Defines an optional start (default 1) and an optional separator (default ". ")
    @Test
    void definesParameters() {
        List<ParameterDefinition> parameters = numberLinesTransformation.getParameters();

        assertEquals(2, parameters.size());
        assertEquals("start", parameters.get(0).getName());
        assertEquals(ParameterType.NUMBER, parameters.get(0).getType());
        assertEquals(1, parameters.get(0).getDefaultValue());
        assertEquals("separator", parameters.get(1).getName());
        assertEquals(ParameterType.TEXT, parameters.get(1).getType());
        assertEquals(". ", parameters.get(1).getDefaultValue());
        parameters.forEach(p -> assertFalse(p.isRequired()));
    }

    // Numbers lines from 1 with ". " by default
    @Test
    void numbersLinesByDefault() {
        assertEquals("1. a\n2. b\n3. c", numberLinesTransformation.apply("a\nb\nc", new HashMap<>()));
        assertEquals("1. a\n2. b\n3. c", numberLinesTransformation.apply("a\nb\nc", null));
    }

    // Uses a custom start number and separator
    @Test
    void usesCustomStartAndSeparator() {
        assertEquals("0: a\n1: b", numberLinesTransformation.apply("a\nb", params(0, ": ")));
    }

    // Start can be sent as a string
    @Test
    void acceptsStartAsString() {
        assertEquals("10. a\n11. b", numberLinesTransformation.apply("a\nb", params("10", null)));
    }

    // Empty lines are numbered too
    @Test
    void numbersEmptyLines() {
        assertEquals("1. a\n2. \n3. b", numberLinesTransformation.apply("a\n\nb", null));
    }

    // Handles CRLF line endings
    @Test
    void handlesCrlf() {
        assertEquals("1. a\n2. b", numberLinesTransformation.apply("a\r\nb", null));
    }

    // Returns an empty string for empty or null input
    @Test
    void returnsEmptyStringForEmptyOrNullInput() {
        assertEquals("", numberLinesTransformation.apply("", null));
        assertEquals("", numberLinesTransformation.apply(null, null));
    }
}
