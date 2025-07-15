package com.example.productmanagement.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ResponseEntity<Object> handleExchangeRateUnavailable(ExchangeRateUnavailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "Exchange rate unavailable");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // @Valid
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            InvalidDataAccessApiUsageException.class
    })
    public ResponseEntity<Map<String, Object>> handleValidationException(Exception ex) {
        Map<String, Object> response = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
            String message = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            response.put("message", message);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Catch-all handler (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralError(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}