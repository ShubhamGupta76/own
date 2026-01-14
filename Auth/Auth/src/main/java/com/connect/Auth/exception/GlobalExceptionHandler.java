package com.connect.Auth.exception;

import com.connect.Auth.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
   
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String message = "Validation failed: " + errors.toString();
        log.error("Validation error: {}", message);
        
        AuthResponse errorResponse = AuthResponse.builder()
                .message(message)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
   
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        AuthResponse errorResponse = AuthResponse.builder()
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        AuthResponse errorResponse = AuthResponse.builder()
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

