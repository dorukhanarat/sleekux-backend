package sleekux_backend.core.transformation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sleekux_backend.core.model.ParameterDefinition;
import sleekux_backend.core.model.ParameterType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TrimTransformationTest {

    private TrimTransformation trimTransformation;

    @BeforeEach
    void setUp() {
        trimTransformation = new TrimTransformation();
    }

    private Map<String, Object> mode(Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", value);
        return params;
    }

    // Has key "trim" and display name "Trim Whitespace"
    @Test
    void hasExpectedKeyAndDisplayName() {
        assertEquals("trim", trimTransformation.getKey());
        assertEquals("Trim Whitespace", trimTransformation.getDisplayName());
    }

    // Defines an optional mode parameter that defaults to "both"
    @Test
    void definesModeParameter() {
        List<ParameterDefinition> parameters = trimTransformation.getParameters();

        assertEquals(1, parameters.size());
        assertEquals("mode", parameters.get(0).getName());
        assertEquals(ParameterType.TEXT, parameters.get(0).getType());
        assertFalse(parameters.get(0).isRequired());
        assertEquals("both", parameters.get(0).getDefaultValue());
    }

    // Mode "both" trims whitespace from both ends
    @Test
    void trimsBothEnds() {
        assertEquals("Hello", trimTransformation.apply("  Hello  ", mode("both")));
    }

    // Mode "start" trims only leading whitespace
    @Test
    void trimsStartOnly() {
        assertEquals("Hello  ", trimTransformation.apply("  Hello  ", mode("start")));
    }

    // Mode "end" trims only trailing whitespace
    @Test
    void trimsEndOnly() {
        assertEquals("  Hello", trimTransformation.apply("  Hello  ", mode("end")));
    }

    // Mode is case-insensitive
    @Test
    void modeIsCaseInsensitive() {
        assertEquals("Hello  ", trimTransformation.apply("  Hello  ", mode("START")));
        assertEquals("  Hello", trimTransformation.apply("  Hello  ", mode("End")));
    }

    // Trims both ends when the mode is missing, null or the map is null
    @Test
    void defaultsToBoth() {
        assertEquals("Hello", trimTransformation.apply("  Hello  ", new HashMap<>()));
        assertEquals("Hello", trimTransformation.apply("  Hello  ", mode(null)));
        assertEquals("Hello", trimTransformation.apply("  Hello  ", null));
    }

    // Trims both ends for an unknown mode
    @Test
    void unknownModeTrimsBoth() {
        assertEquals("Hello", trimTransformation.apply("  Hello  ", mode("middle")));
    }

    // Trims tabs and newlines as well as spaces
    @Test
    void trimsTabsAndNewlines() {
        assertEquals("Hello", trimTransformation.apply("\t\n Hello \n\t", mode("both")));
        assertEquals("Hello \n", trimTransformation.apply("\t Hello \n", mode("start")));
        assertEquals("\t Hello", trimTransformation.apply("\t Hello \n", mode("end")));
    }

    // Trims Unicode whitespace the same way in every mode
    @Test
    void trimsUnicodeWhitespaceInEveryMode() {
        String emSpace = String.valueOf((char) 0x2003);
        String input = emSpace + "Hello" + emSpace;

        assertEquals("Hello", trimTransformation.apply(input, mode("both")));
        assertEquals("Hello" + emSpace, trimTransformation.apply(input, mode("start")));
        assertEquals(emSpace + "Hello", trimTransformation.apply(input, mode("end")));
    }

    // Keeps whitespace inside the text
    @Test
    void keepsInnerWhitespace() {
        assertEquals("Hello   World", trimTransformation.apply("  Hello   World  ", mode("both")));
    }

    // Returns an empty string for whitespace-only input
    @Test
    void returnsEmptyForWhitespaceOnly() {
        assertEquals("", trimTransformation.apply("   ", mode("both")));
        assertEquals("", trimTransformation.apply("   ", mode("start")));
        assertEquals("", trimTransformation.apply("   ", mode("end")));
    }

    // Returns an empty string for null input
    @Test
    void returnsEmptyStringForNullInput() {
        assertEquals("", trimTransformation.apply(null, mode("both")));
    }
}
