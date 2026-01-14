package com.connect.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting filter for API Gateway
 * Uses token bucket algorithm
 * For production, consider using Redis-based rate limiting
 */
@Component
@Slf4j
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60000; // 1 minute

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientIdentifier(exchange);
            RateLimitBucket bucket = buckets.computeIfAbsent(clientId, k -> new RateLimitBucket(requestsPerMinute));

            if (bucket.tryConsume()) {
                return chain.filter(exchange);
            } else {
                log.warn("Rate limit exceeded for client: {}", clientId);
                return handleRateLimitExceeded(exchange);
            }
        };
    }

    private String getClientIdentifier(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        String ip = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
        return "ip:" + ip;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.getHeaders().add("Retry-After", "60");

        String body = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private static class RateLimitBucket {
        private final int maxRequests;
        private final AtomicInteger requests;
        private volatile long windowStart;

        RateLimitBucket(int maxRequests) {
            this.maxRequests = maxRequests;
            this.requests = new AtomicInteger(0);
            this.windowStart = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart >= WINDOW_MS) {
                requests.set(0);
                windowStart = now;
            }
            return requests.incrementAndGet() <= maxRequests;
        }
    }

    public static class Config {
        // Configuration properties if needed
    }
}

