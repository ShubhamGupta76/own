package com.connect.Chat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for Chat Service
 * Handles exceptions and returns appropriate HTTP status codes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException occurred: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        
        // Determine appropriate status code based on error message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = e.getMessage();
        
        if (message != null) {
            if (message.contains("Missing or invalid authorization header") || 
                message.contains("Invalid token") ||
                message.contains("Unauthorized")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (message.contains("not found") || 
                      message.contains("does not exist")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("Access denied") || 
                      message.contains("Forbidden")) {
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("Organization not found")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        errorResponse.put("status", status.value());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
