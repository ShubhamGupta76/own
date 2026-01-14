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


@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

    
        log.info("Incoming request: {} {} | Request-ID: {} | Remote: {}",
                request.getMethod(),
                request.getURI().getPath(),
                requestId,
                request.getRemoteAddress());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
       
        return -200;
    }
}
