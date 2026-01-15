package com.connect.API.Gateway.filter;

import com.connect.API.Gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_PATH = "/auth/";
    private static final String WS_PATH = "/ws/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        
        if (path.startsWith(AUTH_PATH)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        
        if (path.startsWith(WS_PATH)) {
            log.debug("WebSocket endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

       
        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        
        
        log.info("=== JWT Filter Debug ===");
        log.info("Path: {}", path);
        log.info("Authorization header present: {}", authHeader != null);
        log.info("Authorization header value: {}", authHeader != null ? (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader) : "null");
        log.info("All headers: {}", request.getHeaders().keySet());

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for path: {}. Header value: {}", path, authHeader);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        // Remove quotes if present (Postman sometimes adds quotes to variables)
        token = token.trim().replaceAll("^\"|\"$", "");
        log.info("Extracted token length: {}", token.length());
        log.info("Token preview: {}...", token.length() > 20 ? token.substring(0, 20) : token);

       
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token for path: {}. Token validation failed.", path);
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

       
        try {
            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            Long organizationId = jwtUtil.extractOrganizationId(token);

            
            if (userId == null) {
                log.warn("JWT token missing userId claim");
                return onError(exchange, "Invalid token: missing user information", HttpStatus.UNAUTHORIZED);
            }
            if (email == null || email.isEmpty()) {
                log.warn("JWT token missing email claim");
                return onError(exchange, "Invalid token: missing email information", HttpStatus.UNAUTHORIZED);
            }
            if (role == null || role.isEmpty()) {
                log.warn("JWT token missing role claim");
                return onError(exchange, "Invalid token: missing role information", HttpStatus.UNAUTHORIZED);
            }

            // CRITICAL: EMPLOYEE and MANAGER roles MUST have organizationId
            // ADMIN users may not have organizationId initially (before creating organization)
            // But most service endpoints require organizationId, so they'll get appropriate errors from services
            if (("EMPLOYEE".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) && 
                (organizationId == null || organizationId == 0)) {
                log.warn("JWT token missing organizationId for role: {}", role);
                return onError(exchange, "Invalid token: missing organization context for " + role + " role", HttpStatus.FORBIDDEN);
            }

           
            exchange.getAttributes().put("X-User-Id", userId.toString());
            exchange.getAttributes().put("X-User-Email", email);
            exchange.getAttributes().put("X-User-Role", role);
            if (organizationId != null) {
                exchange.getAttributes().put("X-Organization-Id", organizationId.toString());
            }

           
            ServerHttpRequest decoratedRequest = new org.springframework.http.server.reactive.ServerHttpRequestDecorator(request) {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(super.getHeaders());
                    headers.set("X-User-Id", userId.toString());
                    headers.set("X-User-Email", email);
                    headers.set("X-User-Role", role);
                    if (organizationId != null) {
                        headers.set("X-Organization-Id", organizationId.toString());
                    }
                    return headers;
                }
            };

            log.debug("JWT validated successfully for user: {} (role: {}, orgId: {}) on path: {}",
                    email, role, organizationId, path);

            return chain.filter(exchange.mutate().request(decoratedRequest).build());
        } catch (Exception e) {
            log.error("Error processing JWT token. Message: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Stack trace:", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return onError(exchange, "Error processing authentication token: " + errorMsg, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        try {
            response.getHeaders().add("Content-Type", "application/json");
        } catch (Exception e) {
           
            log.debug("Could not add headers to response: {}", e.getMessage());
        }

        String errorBody = String.format("{\"error\": \"%s\", \"status\": %d, \"path\": \"%s\"}",
                message, status.value(), exchange.getRequest().getURI().getPath());

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }

    @Override
    public int getOrder() {
      
        return -100;
    }
}
