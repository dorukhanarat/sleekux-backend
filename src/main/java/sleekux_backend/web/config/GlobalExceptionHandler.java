package sleekux_backend.web.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sleekux_backend.web.dto.ErrorResponseDto;

import java.util.regex.PatternSyntaxException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Invalid client request: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PatternSyntaxException.class)
    public ResponseEntity<ErrorResponseDto> handlePatternSyntaxException(
            PatternSyntaxException ex,
            HttpServletRequest request) {

        log.warn("Regex compilation error: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid regular expression: " + ex.getDescription(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Unreadable request body: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        if(ex instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            log.warn("Client error: {}", ex.getMessage());
            return buildResponse(errorResponse.getStatusCode(), errorResponse.getBody().getDetail(), request);
        }

        log.error("Unhandled server error: ", ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatusCode status, String message, HttpServletRequest request) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String reasonPhrase = resolved != null ? resolved.getReasonPhrase() : String.valueOf(status.value());

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                status.value(),
                reasonPhrase,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponseDto);
    }

}
