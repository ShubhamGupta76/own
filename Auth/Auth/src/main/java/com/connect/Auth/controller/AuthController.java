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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin and Employee authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final EmployeeAuthService employeeAuthService;
    private final com.connect.Auth.service.MigrationService migrationService;

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

    @PostMapping("/admin/organization-id")
    @Operation(summary = "Update admin organizationId", description = "Internal endpoint called by User Service to sync organizationId after organization creation.")
    public ResponseEntity<Map<String, String>> updateAdminOrganizationId(@RequestBody Map<String, Object> request) {
        try {
            Long adminId = Long.valueOf(request.get("adminId").toString());
            Long organizationId = Long.valueOf(request.get("organizationId").toString());
            
            authService.updateOrganizationId(adminId, organizationId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Organization ID updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/migrate/organization-ids")
    @Operation(summary = "Migrate organization IDs", description = "One-time migration endpoint to fix admins with null organizationId by syncing from User service.")
    public ResponseEntity<Map<String, Object>> migrateOrganizationIds() {
        try {
            Map<String, Object> result = migrationService.migrateOrganizationIds();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
