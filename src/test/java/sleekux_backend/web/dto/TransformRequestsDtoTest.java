package sleekux_backend.web.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransformRequestsDtoTest {

    // The empty constructor leaves all fields null
    @Test
    void emptyConstructorLeavesFieldsNull() {
        TransformRequestsDto dto = new TransformRequestsDto();

        assertNull(dto.getText());
        assertNull(dto.getSteps());
    }

    // The full constructor sets the text and steps
    @Test
    void constructorSetsFields() {
        List<StepDto> steps = List.of(new StepDto("uppercase", null));

        TransformRequestsDto dto = new TransformRequestsDto("hello", steps);

        assertEquals("hello", dto.getText());
        assertSame(steps, dto.getSteps());
    }

    // Setters update the values returned by the getters
    @Test
    void settersUpdateFields() {
        TransformRequestsDto dto = new TransformRequestsDto();
        List<StepDto> steps = List.of(new StepDto("trim", null));

        dto.setText("hello");
        dto.setSteps(steps);

        assertEquals("hello", dto.getText());
        assertSame(steps, dto.getSteps());
    }
}
