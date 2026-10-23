package sleekux_backend.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineResultTest {

    private final StepExecutionResult step = new StepExecutionResult(1, "uppercase", "To Uppercase", Map.of(), "hello", "HELLO", 0);

    // Getters return the values passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        PipelineResult result = new PipelineResult("hello", "HELLO", List.of(step));

        assertEquals("hello", result.getOriginalText());
        assertEquals("HELLO", result.getFinalResult());
        assertEquals(List.of(step), result.getHistory());
    }

    // A null history becomes an empty list
    @Test
    void nullHistoryBecomesEmptyList() {
        PipelineResult result = new PipelineResult("a", "a", null);

        assertNotNull(result.getHistory());
        assertTrue(result.getHistory().isEmpty());
    }

    // The history cannot be modified through the result
    @Test
    void historyIsUnmodifiable() {
        PipelineResult result = new PipelineResult("hello", "HELLO", List.of(step));

        assertThrows(UnsupportedOperationException.class, () -> result.getHistory().add(step));
        assertThrows(UnsupportedOperationException.class, () -> result.getHistory().clear());
    }

    // Results with the same text and history are equal and have the same hash code
    @Test
    void equalsWhenAllFieldsMatch() {
        PipelineResult first = new PipelineResult("hello", "HELLO", List.of(step));
        PipelineResult second = new PipelineResult("hello", "HELLO", List.of(step));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    // Results with different original text, final result or history are not equal
    @Test
    void notEqualWhenAnyFieldDiffers() {
        PipelineResult base = new PipelineResult("hello", "HELLO", List.of(step));

        assertNotEquals(base, new PipelineResult("other", "HELLO", List.of(step)));
        assertNotEquals(base, new PipelineResult("hello", "OTHER", List.of(step)));
        assertNotEquals(base, new PipelineResult("hello", "HELLO", List.of()));
    }

    // A null history is treated the same as an empty history
    @Test
    void nullHistoryEqualsEmptyHistory() {
        assertEquals(new PipelineResult("a", "a", List.of()), new PipelineResult("a", "a", null));
    }

    // Is not equal to null or to an object of another type
    @Test
    void notEqualToNullOrOtherType() {
        PipelineResult result = new PipelineResult("a", "a", List.of());

        assertNotEquals(null, result);
        assertNotEquals("a", result);
    }

    // toString includes the original text, final result and history
    @Test
    void toStringContainsAllFields() {
        String text = new PipelineResult("hello", "HELLO", List.of(step)).toString();

        assertTrue(text.contains("originalText='hello'"));
        assertTrue(text.contains("finalResult='HELLO'"));
        assertTrue(text.contains("history=[StepExecutionResult{"));
    }

    // toString shows an empty history when none is given
    @Test
    void toStringShowsEmptyHistory() {
        assertTrue(new PipelineResult("a", "a", null).toString().contains("history=[]"));
    }
}
