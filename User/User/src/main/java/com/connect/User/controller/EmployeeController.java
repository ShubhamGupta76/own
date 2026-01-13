package com.connect.User.controller;

import com.connect.User.dto.EmployeeLoginRequest;
import com.connect.User.dto.EmployeeProfileResponse;
import com.connect.User.dto.EmployeeValidationResponse;
import com.connect.User.service.EmployeeService;
import com.connect.User.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for employee operations
 * Internal endpoint for Auth Service and employee profile endpoints
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Employee login validation and profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    
    /**
     * Internal endpoint for Auth Service to validate employee credentials
     * POST /api/employees/validate
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate employee credentials", description = "Internal endpoint for Auth Service to validate employee login credentials.")
    public ResponseEntity<EmployeeValidationResponse> validateEmployeeCredentials(
            @Valid @RequestBody EmployeeLoginRequest request) {
        EmployeeValidationResponse response = employeeService.validateEmployeeCredentials(request);
        
        if (!response.getIsValid()) {
            return ResponseEntity.status(401).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get employee profile
     * GET /api/employees/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get my profile", description = "Retrieves the logged-in employee's profile. Employee can only access their own profile.")
    public ResponseEntity<EmployeeProfileResponse> getMyProfile(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            EmployeeProfileResponse profile = employeeService.getEmployeeProfile(userId, userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Create or update employee profile on first login
     * POST /api/employees/profile
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create/update profile", description = "Creates or updates employee profile on first login. Sets display name and marks as not first login.")
    public ResponseEntity<EmployeeProfileResponse> createOrUpdateProfile(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            EmployeeProfileResponse profile = employeeService.createOrUpdateEmployeeProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Extract user ID from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    /**
     * Extract token from request
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
}

