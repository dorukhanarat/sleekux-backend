package sleekux_backend.web.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import sleekux_backend.web.dto.ErrorResponseDto;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/transform");
    }

    // IllegalArgumentException becomes a 400 response with the exception message
    @Test
    void handlesIllegalArgumentException() {
        ResponseEntity<ErrorResponseDto> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Unknown transformation key: x"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Unknown transformation key: x", body.getMessage());
        assertEquals("/api/transform", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    // PatternSyntaxException becomes a 400 response describing the regex problem
    @Test
    void handlesPatternSyntaxException() {
        PatternSyntaxException ex = new PatternSyntaxException("Unclosed group", "(", 1);

        ResponseEntity<ErrorResponseDto> response = handler.handlePatternSyntaxException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Bad Request", body.getError());
        assertEquals("Invalid regular expression: Unclosed group", body.getMessage());
        assertEquals("/api/transform", body.getPath());
    }

    // An unreadable request body becomes a 400 response with a generic message
    @Test
    void handlesHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error at line 1", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponseDto> response = handler.handleHttpMessageNotReadableException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is missing or is not valid JSON.", response.getBody().getMessage());
        assertEquals("Bad Request", response.getBody().getError());
    }

    // Spring client errors keep their own status code instead of becoming a 500
    @Test
    void keepsStatusOfSpringClientErrors() {
        ResponseEntity<ErrorResponseDto> methodResponse = handler.handleGeneralException(
                new HttpRequestMethodNotSupportedException("GET", List.of("POST")), request);
        ResponseEntity<ErrorResponseDto> mediaTypeResponse = handler.handleGeneralException(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)), request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, methodResponse.getStatusCode());
        assertEquals(405, methodResponse.getBody().getStatus());
        assertEquals("Method Not Allowed", methodResponse.getBody().getError());
        assertEquals("Method 'GET' is not supported.", methodResponse.getBody().getMessage());

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mediaTypeResponse.getStatusCode());
        assertEquals("Unsupported Media Type", mediaTypeResponse.getBody().getError());
    }

    // Any other exception becomes a 500 response with a generic message
    @Test
    void handlesGeneralException() {
        ResponseEntity<ErrorResponseDto> response = handler.handleGeneralException(
                new IllegalStateException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("An unexpected error occurred.", body.getMessage());
        assertEquals("/api/transform", body.getPath());
    }

    // The 500 response does not reveal the internal exception message
    @Test
    void generalExceptionDoesNotLeakDetails() {
        ResponseEntity<ErrorResponseDto> response = handler.handleGeneralException(
                new RuntimeException("secret internal detail"), request);

        assertFalse(response.getBody().getMessage().contains("secret"));
    }
}
