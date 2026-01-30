package com.connect.Team.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        
        // Collect all validation errors
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        if (errors.isEmpty()) {
            errors = "Validation failed";
        }
        
        response.put("message", errors);
        
        // Also include field-level errors for debugging
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle constraint violation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle Spring Security AccessDeniedException
     * This occurs when @PreAuthorize fails
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied by Spring Security: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            response.put("message", "Access denied: " + message);
        } else {
            response.put("message", "Access denied: You do not have permission to perform this action. Please ensure you have the required role (ADMIN or MANAGER) and that your account is associated with an organization. If you just registered, try logging out and logging back in.");
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.debug("Handling RuntimeException: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        String message = e.getMessage();
        response.put("message", message != null && !message.isEmpty() ? message : "An error occurred");
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = "Bad Request";
        
        if (message != null && !message.isEmpty()) {
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("not found") || 
                lowerMessage.contains("does not exist")) {
                status = HttpStatus.NOT_FOUND;
                error = "Not Found";
                log.debug("Mapped to 404 Not Found: {}", message);
            } else if (lowerMessage.contains("access denied") || 
                       lowerMessage.contains("permission") || 
                       lowerMessage.contains("unauthorized") ||
                       lowerMessage.contains("can only access") ||
                       lowerMessage.contains("can only manage") ||
                       lowerMessage.contains("only admin and manager") ||
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
}

