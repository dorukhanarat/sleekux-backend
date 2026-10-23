package sleekux_backend.core.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineStepTest {

    // Getters return the key and parameters passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        PipelineStep step = new PipelineStep("append", Map.of("suffix", "!"));

        assertEquals("append", step.getTransformationKey());
        assertEquals(Map.of("suffix", "!"), step.getParameters());
    }

    // A null parameter map becomes an empty map
    @Test
    void nullParametersBecomeEmptyMap() {
        PipelineStep step = new PipelineStep("uppercase", null);

        assertNotNull(step.getParameters());
        assertTrue(step.getParameters().isEmpty());
    }

    // The parameter map cannot be modified through the step
    @Test
    void parametersAreUnmodifiable() {
        Map<String, Object> params = new HashMap<>();
        params.put("suffix", "!");
        PipelineStep step = new PipelineStep("append", params);

        assertThrows(UnsupportedOperationException.class, () -> step.getParameters().put("suffix", "?"));
        assertThrows(UnsupportedOperationException.class, () -> step.getParameters().clear());
    }

    // A null transformation key is rejected
    @Test
    void rejectsNullKey() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new PipelineStep(null, Map.of()));

        assertEquals("transformationKey cannot be null or blank", e.getMessage());
    }

    // An empty or whitespace-only transformation key is rejected
    @Test
    void rejectsBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> new PipelineStep("", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new PipelineStep("   ", Map.of()));
    }

    // toString includes the key and parameters
    @Test
    void toStringContainsKeyAndParameters() {
        String text = new PipelineStep("append", Map.of("suffix", "!")).toString();

        assertTrue(text.contains("transformationKey='append'"));
        assertTrue(text.contains("suffix=!"));
    }
}
