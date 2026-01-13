package com.connect.Chat.security;

import com.connect.Chat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * JWT Channel Interceptor for WebSocket authentication
 * Validates JWT token from WebSocket connection headers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from headers
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    
                    try {
                        // Validate token
                        if (jwtUtil.validateToken(token)) {
                            String role = jwtUtil.extractRole(token);
                            String email = jwtUtil.extractEmail(token);
                            Long userId = jwtUtil.extractUserId(token);
                            
                            // Set authentication
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                            email,
                                            null,
                                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                    );
                            
                            // Store user info in session
                            accessor.setUser(authentication);
                            accessor.getSessionAttributes().put("userId", userId);
                            accessor.getSessionAttributes().put("organizationId", jwtUtil.extractOrganizationId(token));
                            
                            log.info("WebSocket authenticated: userId={}, role={}", userId, role);
                        } else {
                            log.warn("Invalid JWT token in WebSocket connection");
                            throw new RuntimeException("Invalid JWT token");
                        }
                    } catch (Exception e) {
                        log.error("Error validating JWT token in WebSocket: {}", e.getMessage());
                        throw new RuntimeException("Authentication failed: " + e.getMessage());
                    }
                } else {
                    log.warn("Missing or invalid Authorization header in WebSocket connection");
                    throw new RuntimeException("Missing Authorization header");
                }
            } else {
                log.warn("No Authorization header found in WebSocket connection");
                throw new RuntimeException("Missing Authorization header");
            }
        }
        
        return message;
    }
}

