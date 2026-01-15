package com.connect.User.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.debug("Handling RuntimeException: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        
        String message = e.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = "Bad Request";
        
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("not found") || 
                lowerMessage.contains("does not exist") ||
                lowerMessage.contains("user not found") ||
                lowerMessage.contains("organization not found")) {
                status = HttpStatus.NOT_FOUND;
                error = "Not Found";
                log.debug("Mapped to 404 Not Found: {}", message);
            } else if (lowerMessage.contains("access denied") || 
                       lowerMessage.contains("permission") || 
                       lowerMessage.contains("unauthorized") ||
                       lowerMessage.contains("can only access") ||
                       lowerMessage.contains("can only manage") ||
                       lowerMessage.contains("organization context is missing")) {
                status = HttpStatus.FORBIDDEN;
                error = "Forbidden";
                log.debug("Mapped to 403 Forbidden: {}", message);
            } else if (lowerMessage.contains("already exists") || 
                       lowerMessage.contains("duplicate")) {
                status = HttpStatus.CONFLICT;
                error = "Conflict";
                log.debug("Mapped to 409 Conflict: {}", message);
            } else {
                log.debug("Using default 400 Bad Request for: {}", message);
            }
        }
        
        response.put("status", status.value());
        response.put("error", error);
        
        return ResponseEntity.status(status).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Handling generic exception: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

