package sleekux_backend.web.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StepDtoTest {

    // The empty constructor leaves all fields null
    @Test
    void emptyConstructorLeavesFieldsNull() {
        StepDto dto = new StepDto();

        assertNull(dto.getTransformationKey());
        assertNull(dto.getParameters());
    }

    // The full constructor sets the key and parameters
    @Test
    void constructorSetsFields() {
        StepDto dto = new StepDto("append", Map.of("suffix", "!"));

        assertEquals("append", dto.getTransformationKey());
        assertEquals(Map.of("suffix", "!"), dto.getParameters());
    }

    // Setters update the values returned by the getters
    @Test
    void settersUpdateFields() {
        StepDto dto = new StepDto();

        dto.setTransformationKey("trim");
        dto.setParameters(Map.of("mode", "start"));

        assertEquals("trim", dto.getTransformationKey());
        assertEquals(Map.of("mode", "start"), dto.getParameters());
    }
}
