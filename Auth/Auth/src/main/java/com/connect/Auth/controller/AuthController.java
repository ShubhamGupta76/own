package com.connect.Auth.controller;

import com.connect.Auth.dto.AuthResponse;
import com.connect.Auth.dto.LoginRequest;
import com.connect.Auth.dto.RegisterRequest;
import com.connect.Auth.service.AuthService;
import com.connect.Auth.service.EmployeeAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 * Handles admin and employee authentication
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin and Employee authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final EmployeeAuthService employeeAuthService;

    /**
     * Register a new admin
     * POST /api/auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new admin", description = "Creates a new admin account. Organization ID will be set after organization creation.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Login admin
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login admin", description = "Authenticates admin and returns JWT token with userId, email, role, and organizationId")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Login employee
     * POST /api/auth/employee/login
     */
    @PostMapping("/employee/login")
    @Operation(summary = "Login employee", description = "Authenticates employee using company email and password. Returns JWT token with userId, email, role=EMPLOYEE, and organizationId. Auto-creates profile on first login.")
    public ResponseEntity<AuthResponse> loginEmployee(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = employeeAuthService.loginEmployee(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
