package com.connect.Chat.config;

import com.connect.Chat.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat
 * Enables STOMP over WebSocket with JWT authentication
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtChannelInterceptor jwtChannelInterceptor;
    
    /**
     * Configure message broker
     * Uses in-memory broker for simplicity (can be replaced with RabbitMQ/Redis in production)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }
    
    /**
     * Register STOMP endpoints
     * withSockJS() provides both SockJS and plain WebSocket support
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String allowedOrigins = System.getenv("WEBSOCKET_ALLOWED_ORIGINS");
        String[] origins = allowedOrigins != null && !allowedOrigins.isEmpty()
                ? allowedOrigins.split(",")
                : new String[]{"http://localhost:3000", "http://frontend:3000", "http://localhost:5173"};
        
        // SockJS endpoint supports both SockJS and plain WebSocket connections
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns(origins)
                .withSockJS();
    }
    
    /**
     * Configure client inbound channel
     * Add JWT interceptor for authentication
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}

