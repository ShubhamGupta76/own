package com.connect.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestIdHeader = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        final String requestId = (requestIdHeader == null || requestIdHeader.isEmpty()) 
            ? UUID.randomUUID().toString() 
            : requestIdHeader;

        long startTime = System.currentTimeMillis();
    
        log.info("Incoming request: {} {} | Request-ID: {} | Remote: {}",
                request.getMethod(),
                request.getURI().getPath(),
                requestId,
                request.getRemoteAddress());

        return chain.filter(exchange).doOnSuccess(aVoid -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpStatus status = null;
            if (response.getStatusCode() != null) {
                status = HttpStatus.resolve(response.getStatusCode().value());
            }
            long duration = System.currentTimeMillis() - startTime;
            
            if (status != null) {
                if (status.is5xxServerError()) {
                    log.error("Request failed: {} {} | Status: {} | Duration: {}ms | Request-ID: {}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            status.value(),
                            duration,
                            requestId);
                } else if (status.is4xxClientError()) {
                    log.warn("Client error: {} {} | Status: {} | Duration: {}ms | Request-ID: {}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            status.value(),
                            duration,
                            requestId);
                } else {
                    log.debug("Request completed: {} {} | Status: {} | Duration: {}ms | Request-ID: {}",
                            request.getMethod(),
                            request.getURI().getPath(),
                            status.value(),
                            duration,
                            requestId);
                }
            }
        }).doOnError(throwable -> {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request error: {} {} | Error: {} | Duration: {}ms | Request-ID: {}",
                    request.getMethod(),
                    request.getURI().getPath(),
                    throwable.getMessage(),
                    duration,
                    requestId,
                    throwable);
        });
    }

    @Override
    public int getOrder() {
       
        return -200;
    }
}
