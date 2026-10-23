package sleekux_backend.web.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseDtoTest {

    // Getters return the values passed to the constructor
    @Test
    void gettersReturnConstructorValues() {
        ErrorResponseDto dto = new ErrorResponseDto(400, "Bad Request", "Something went wrong", "/api/transform");

        assertEquals(400, dto.getStatus());
        assertEquals("Bad Request", dto.getError());
        assertEquals("Something went wrong", dto.getMessage());
        assertEquals("/api/transform", dto.getPath());
    }

    // The timestamp is set to the time the object was created
    @Test
    void timestampIsCreationTime() {
        Instant before = Instant.now();
        ErrorResponseDto dto = new ErrorResponseDto(500, "Internal Server Error", "x", "/");
        Instant after = Instant.now();

        assertFalse(dto.getTimestamp().isBefore(before));
        assertFalse(dto.getTimestamp().isAfter(after));
    }
}
