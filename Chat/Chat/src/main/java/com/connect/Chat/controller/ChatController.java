package com.connect.Chat.controller;

import com.connect.Chat.dto.MessageResponse;
import com.connect.Chat.dto.SendMessageRequest;
import com.connect.Chat.service.ChatService;
import com.connect.Chat.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Employee chat APIs for sending and receiving messages")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {
    
    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private String getRole(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractRole(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Validate that userId is not null and is positive
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }
    }
    
    /**
     * Send a message
     * POST /api/chat/send
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Send message", description = "Sends a message in a chat room (channel, team, or direct). Supports TEXT, FILE, LINK, EMOJI, GIF types. Message is persisted and broadcast via WebSocket.")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MessageResponse response = chatService.sendMessage(request, userId, role, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get messages for a channel
     * GET /api/chat/channel/{channelId}
     */
    @GetMapping("/channel/{channelId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get channel messages", description = "Retrieves messages for a channel. Supports pagination.")
    public ResponseEntity<List<MessageResponse>> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<MessageResponse> messages = chatService.getChannelMessages(channelId, organizationId, page, size);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get messages for direct chat with a user
     * GET /api/chat/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get user messages", description = "Retrieves messages from direct chat with a user. Supports pagination.")
    public ResponseEntity<List<MessageResponse>> getUserMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<MessageResponse> messages = chatService.getUserMessages(userId, currentUserId, organizationId, page, size);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get messages for direct chat (alternative endpoint)
     * GET /api/chat/direct/{userId}
     */
    @GetMapping("/direct/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get direct chat messages", description = "Retrieves messages from direct 1-to-1 chat with a user. Supports pagination.")
    public ResponseEntity<List<MessageResponse>> getDirectMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        try {
            // Validate userId parameter
            validateUserId(userId);
            
            Long currentUserId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (currentUserId == null || currentUserId <= 0) {
                throw new RuntimeException("Invalid current user ID");
            }
            
            if (organizationId == null || organizationId <= 0) {
                throw new RuntimeException("Organization not found in token");
            }
            
            // Prevent users from chatting with themselves
            if (userId.equals(currentUserId)) {
                throw new RuntimeException("Cannot create direct chat with yourself");
            }
            
            List<MessageResponse> messages = chatService.getUserMessages(userId, currentUserId, organizationId, page, size);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Error in getDirectMessages: {}", e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}

