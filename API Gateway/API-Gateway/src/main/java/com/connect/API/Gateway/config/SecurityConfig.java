package com.connect.API.Gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                // Disable Spring Security CORS - Gateway global CORS handles it
                .cors(cors -> cors.disable())
                .authorizeExchange(exchanges -> exchanges
                        // Permit all OPTIONS requests for CORS preflight (must be before other rules)
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Permit all auth endpoints
                        .pathMatchers("/auth/**").permitAll()
                        // Permit actuator endpoints
                        .pathMatchers("/actuator/**").permitAll()
                        // All other requests - JWT filter will handle authentication
                        .anyExchange().permitAll()
                );

        return http.build();
    }
}
