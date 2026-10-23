package sleekux_backend.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StepExecutionResultTest {

    private StepExecutionResult create(long executionTime) {
        return new StepExecutionResult(1, "uppercase", "To Uppercase", Map.of(), "hello", "HELLO", executionTime);
    }

    // Getters return the values passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        StepExecutionResult result = new StepExecutionResult(2, "append", "Append Text", Map.of("suffix", "!"), "Hi", "Hi!", 7);

        assertEquals(2, result.getStepIndex());
        assertEquals("append", result.getTransformationKey());
        assertEquals("Append Text", result.getTransformationName());
        assertEquals(Map.of("suffix", "!"), result.getParameters());
        assertEquals("Hi", result.getInputSnapshot());
        assertEquals("Hi!", result.getOutputSnapshot());
        assertEquals(7, result.getExecutionTime());
    }

    // Results with the same fields are equal and have the same hash code
    @Test
    void equalsWhenAllFieldsMatch() {
        assertEquals(create(5), create(5));
        assertEquals(create(5).hashCode(), create(5).hashCode());
    }

    // Execution time is not part of equality
    @Test
    void equalityIgnoresExecutionTime() {
        assertEquals(create(0), create(250));
        assertEquals(create(0).hashCode(), create(250).hashCode());
    }

    // Results that differ in any other field are not equal
    @Test
    void notEqualWhenAnyOtherFieldDiffers() {
        StepExecutionResult base = create(0);

        assertNotEquals(base, new StepExecutionResult(2, "uppercase", "To Uppercase", Map.of(), "hello", "HELLO", 0));
        assertNotEquals(base, new StepExecutionResult(1, "lowercase", "To Uppercase", Map.of(), "hello", "HELLO", 0));
        assertNotEquals(base, new StepExecutionResult(1, "uppercase", "Other Name", Map.of(), "hello", "HELLO", 0));
        assertNotEquals(base, new StepExecutionResult(1, "uppercase", "To Uppercase", Map.of("a", 1), "hello", "HELLO", 0));
        assertNotEquals(base, new StepExecutionResult(1, "uppercase", "To Uppercase", Map.of(), "other", "HELLO", 0));
        assertNotEquals(base, new StepExecutionResult(1, "uppercase", "To Uppercase", Map.of(), "hello", "OTHER", 0));
    }

    // Is not equal to null or to an object of another type
    @Test
    void notEqualToNullOrOtherType() {
        assertNotEquals(null, create(0));
        assertNotEquals("uppercase", create(0));
    }

    // toString includes every field, including execution time
    @Test
    void toStringContainsAllFields() {
        String text = create(42).toString();

        assertTrue(text.contains("stepIndex=1"));
        assertTrue(text.contains("transformationKey='uppercase'"));
        assertTrue(text.contains("transformationName='To Uppercase'"));
        assertTrue(text.contains("inputSnapshot='hello'"));
        assertTrue(text.contains("outputSnapshot='HELLO'"));
        assertTrue(text.contains("executionTime=42"));
    }
}
