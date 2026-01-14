package com.connect.Chat.controller;

import com.connect.Chat.entity.ChatPolicy;
import com.connect.Chat.service.ChatPolicyService;
import com.connect.Chat.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat/policies")
@RequiredArgsConstructor
@Tag(name = "Chat Policy Management", description = "Admin APIs for controlling chat feature")
@SecurityRequirement(name = "bearerAuth")
public class ChatPolicyController {
    
    private final ChatPolicyService chatPolicyService;
    private final JwtUtil jwtUtil;
    
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
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get chat policy", description = "Retrieves chat policy status for the organization.")
    public ResponseEntity<ChatPolicy> getChatPolicy(HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(chatPolicyService.getChatPolicy(organizationId));
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update chat policy", description = "Enables or disables chat feature for the organization.")
    public ResponseEntity<ChatPolicy> updateChatPolicy(
            @RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            throw new RuntimeException("Enabled status is required");
        }
        return ResponseEntity.ok(chatPolicyService.updateChatPolicy(organizationId, enabled));
    }
}

