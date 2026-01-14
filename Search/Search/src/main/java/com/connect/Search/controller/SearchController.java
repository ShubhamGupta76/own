package com.connect.Search.controller;

import com.connect.Search.dto.SearchResult;
import com.connect.Search.service.SearchService;
import com.connect.Search.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Admin read APIs for searching users, teams, and channels")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {
    
    private final SearchService searchService;
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
    @Operation(summary = "Search", description = "Searches users, teams, and channels in the organization. Type can be USER, TEAM, CHANNEL, or null for all.")
    public ResponseEntity<List<SearchResult>> search(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(searchService.search(organizationId, query, type));
    }
}

