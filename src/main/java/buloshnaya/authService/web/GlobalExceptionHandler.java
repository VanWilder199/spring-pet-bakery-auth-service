package buloshnaya.authService.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private  static final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        logger.error("An unexpected error occurred", e);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
               null,
                "INTERNAL_SERVER_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponseDto);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        logger.warn("Email conflict: {}", e.getMessage());
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                e.getMessage(),
                "CONFLICT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponseDto);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound (EntityNotFoundException e) {
        logger.error("Entity not found", e);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                e.getMessage(),
                "NOT_FOUND"
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponseDto);
    }

    @ExceptionHandler(value = {
            IllegalArgumentException.class,
            IllegalStateException.class,
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest (Exception e) {
        logger.error("Bad request", e);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                e.getMessage(),
                "BAD_REQUEST"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponseDto(message, "BAD_REQUEST"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        logger.warn("Invalid refresh token: {}", e.getMessage());
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                e.getMessage(),
                "UNAUTHORIZED"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponseDto);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException e) {
        logger.warn("Invalid credentials: {}", e.getMessage());
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                e.getMessage(),
                "UNAUTHORIZED"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponseDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("Data already exist", "CONFLICT"));
    }
}
