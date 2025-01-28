package dev.eventmanager.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleCommonException(Exception ex) {
        log.error("Handle common exception", ex);

        ErrorMessage errorMessage = new ErrorMessage(
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Handle validation exception ", ex);
        String detailedMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorMessage errorMessage = new ErrorMessage(
                "Request validation error",
                detailedMessage,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Handle illegal argument exception ", ex);

        ErrorMessage errorMessage = new ErrorMessage(
                "Illegal argument error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }

    @ExceptionHandler(value = {NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleNoSuchElementException(Exception ex) {
        log.error("Handle not found error ", ex);

        ErrorMessage errorMessage = new ErrorMessage(
                "Not found error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.
                status(HttpStatus.NOT_FOUND)
                .body(errorMessage);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.error("Handle AuthorizationDenied Exception ", ex);
        ErrorMessage errorMessage = new ErrorMessage(
                "Authorization error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN.value())
                .body(errorMessage);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException ex) {
        log.error("Handle Authentication Exception ", ex);
        ErrorMessage errorMessage = new ErrorMessage(
                "Authentication error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorMessage);
    }
}