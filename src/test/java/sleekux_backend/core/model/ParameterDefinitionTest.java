package sleekux_backend.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterDefinitionTest {

    private ParameterDefinition create() {
        return new ParameterDefinition("oldValue", "Target String", ParameterType.TEXT, true, "", "Text to replace");
    }

    // Getters return the values passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        ParameterDefinition definition = new ParameterDefinition("count", "Count", ParameterType.NUMBER, false, 5, "Enter a number");

        assertEquals("count", definition.getName());
        assertEquals("Count", definition.getLabel());
        assertEquals(ParameterType.NUMBER, definition.getType());
        assertFalse(definition.isRequired());
        assertEquals(5, definition.getDefaultValue());
        assertEquals("Enter a number", definition.getPlaceholder());
    }

    // Allows null default value and placeholder
    @Test
    void allowsNullDefaultValueAndPlaceholder() {
        ParameterDefinition definition = new ParameterDefinition("flag", "Flag", ParameterType.BOOLEAN, false, null, null);

        assertNull(definition.getDefaultValue());
        assertNull(definition.getPlaceholder());
    }

    // Is equal to itself
    @Test
    void equalsItself() {
        ParameterDefinition definition = create();

        assertEquals(definition, definition);
    }

    // Definitions with the same fields are equal and have the same hash code
    @Test
    void equalsWhenAllFieldsMatch() {
        ParameterDefinition first = create();
        ParameterDefinition second = create();

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    // Definitions with different names are not equal
    @Test
    void notEqualWhenNameDiffers() {
        ParameterDefinition other = new ParameterDefinition("other", "Target String", ParameterType.TEXT, true, "", "Text to replace");

        assertNotEquals(create(), other);
    }

    // Definitions with different labels are not equal
    @Test
    void notEqualWhenLabelDiffers() {
        ParameterDefinition other = new ParameterDefinition("oldValue", "Other Label", ParameterType.TEXT, true, "", "Text to replace");

        assertNotEquals(create(), other);
    }

    // Definitions with different types are not equal
    @Test
    void notEqualWhenTypeDiffers() {
        ParameterDefinition other = new ParameterDefinition("oldValue", "Target String", ParameterType.REGEX, true, "", "Text to replace");

        assertNotEquals(create(), other);
    }

    // Definitions with different required flags are not equal
    @Test
    void notEqualWhenRequiredDiffers() {
        ParameterDefinition other = new ParameterDefinition("oldValue", "Target String", ParameterType.TEXT, false, "", "Text to replace");

        assertNotEquals(create(), other);
    }

    // Is not equal to null or to an object of another type
    @Test
    void notEqualToNullOrOtherType() {
        ParameterDefinition definition = create();

        assertNotEquals(null, definition);
        assertNotEquals("oldValue", definition);
    }

    // Default value and placeholder are not part of equality
    @Test
    void equalityIgnoresDefaultValueAndPlaceholder() {
        ParameterDefinition other = new ParameterDefinition("oldValue", "Target String", ParameterType.TEXT, true, "something", "Different placeholder");

        assertEquals(create(), other);
        assertEquals(create().hashCode(), other.hashCode());
    }

    // toString includes name, label, type and required flag
    @Test
    void toStringContainsIdentifyingFields() {
        String text = create().toString();

        assertTrue(text.contains("name='oldValue'"));
        assertTrue(text.contains("label='Target String'"));
        assertTrue(text.contains("type=TEXT"));
        assertTrue(text.contains("required=true"));
    }
}
