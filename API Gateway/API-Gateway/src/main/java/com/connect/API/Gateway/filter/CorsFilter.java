package com.connect.API.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
public class CorsFilter implements GlobalFilter, Ordered {

    private static final String ALLOWED_ORIGINS = "http://localhost:3000,http://frontend:3000,http://localhost:4200";
    private static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,PATCH,OPTIONS";
    private static final String ALLOWED_HEADERS = "Authorization,Content-Type,X-Request-Id";
    private static final int MAX_AGE = 3600;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (CorsUtils.isCorsRequest(request)) {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
            if (origin != null && isAllowedOrigin(origin)) {
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }

            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(MAX_AGE));

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }
        }

        return chain.filter(exchange);
    }

    private boolean isAllowedOrigin(String origin) {
        return origin.equals("http://localhost:3000") ||
                origin.equals("http://frontend:3000") ||
                origin.equals("http://localhost:4200");
    }

    @Override
    public int getOrder() {
        
        return -300;
    }
}
