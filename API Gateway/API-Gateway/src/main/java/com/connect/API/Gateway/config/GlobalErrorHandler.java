package com.connect.API.Gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global error handler for Spring Cloud Gateway
 * Handles connection errors, timeouts, and other exceptions
 */
@Component
@Order(-2)
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log.error("Global error handler caught exception: {}", ex.getMessage(), ex);
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Request path: {}", exchange.getRequest().getURI().getPath());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "Internal Server Error";
        String errorCode = "INTERNAL_ERROR";

        // Determine status and error message based on exception type
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            status = HttpStatus.resolve(statusCode.value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            errorMessage = responseStatusException.getReason() != null 
                ? responseStatusException.getReason() 
                : status.getReasonPhrase();
        } else {
            String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            String className = ex.getClass().getSimpleName();

            // Connection errors
            if (message.contains("connection refused") || 
                message.contains("connection reset") ||
                className.contains("ConnectException")) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                errorMessage = "Backend service is not available. Please ensure all services are running.";
                errorCode = "SERVICE_UNAVAILABLE";
                log.warn("Connection error detected: {}", ex.getMessage());
            }
            // Timeout errors
            else if (message.contains("timeout") || 
                     message.contains("read timeout") ||
                     className.contains("TimeoutException")) {
                status = HttpStatus.GATEWAY_TIMEOUT;
                errorMessage = "Request timed out. The backend service took too long to respond.";
                errorCode = "GATEWAY_TIMEOUT";
                log.warn("Timeout error detected: {}", ex.getMessage());
            }
            // Network errors
            else if (message.contains("network") || 
                     message.contains("unresolved") ||
                     className.contains("UnknownHostException")) {
                status = HttpStatus.BAD_GATEWAY;
                errorMessage = "Network error. Unable to reach backend service.";
                errorCode = "BAD_GATEWAY";
                log.warn("Network error detected: {}", ex.getMessage());
            }
        }

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", errorMessage);
        errorResponse.put("path", exchange.getRequest().getURI().getPath());

        // Write error response
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            String fallbackResponse = String.format(
                "{\"error\":\"%s\",\"status\":%d,\"path\":\"%s\"}",
                errorMessage, status.value(), exchange.getRequest().getURI().getPath()
            );
            DataBuffer buffer = bufferFactory.wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}

