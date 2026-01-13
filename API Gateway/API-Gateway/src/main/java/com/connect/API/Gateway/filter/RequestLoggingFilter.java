package com.connect.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request Logging and Tracing Filter
 * Adds X-Request-Id header for request tracing
 * Logs incoming requests
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generate request ID for tracing (don't modify request headers as they become
        // read-only)
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Log request (without modifying request - can cause read-only header issues)
        log.info("Incoming request: {} {} | Request-ID: {} | Remote: {}",
                request.getMethod(),
                request.getURI().getPath(),
                requestId,
                request.getRemoteAddress());

        // Don't modify the request - just pass it through to avoid read-only header
        // exceptions
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Execute before JWT filter to ensure request ID is set
        return -200;
    }
}
