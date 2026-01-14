package com.connect.File.controller;

import com.connect.File.entity.FilePolicy;
import com.connect.File.service.FilePolicyService;
import com.connect.File.util.JwtUtil;
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
@RequestMapping("/api/v1/files/policies")
@RequiredArgsConstructor
@Tag(name = "File Policy Management", description = "Admin APIs for controlling file sharing feature")
@SecurityRequirement(name = "bearerAuth")
public class FilePolicyController {
    
    private final FilePolicyService filePolicyService;
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
    @Operation(summary = "Get file policy", description = "Retrieves file sharing policy configuration for the organization.")
    public ResponseEntity<FilePolicy> getFilePolicy(HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(filePolicyService.getFilePolicy(organizationId));
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update file policy", description = "Updates file sharing policy (enable/disable, max file size, allowed file types).")
    public ResponseEntity<FilePolicy> updateFilePolicy(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        
        Boolean enabled = request.get("enabled") != null ? (Boolean) request.get("enabled") : null;
        Integer maxFileSizeMb = request.get("maxFileSizeMb") != null ? 
                ((Number) request.get("maxFileSizeMb")).intValue() : null;
        String allowedFileTypes = request.get("allowedFileTypes") != null ? 
                (String) request.get("allowedFileTypes") : null;
        
        return ResponseEntity.ok(filePolicyService.updateFilePolicy(organizationId, enabled, maxFileSizeMb, allowedFileTypes));
    }
}

