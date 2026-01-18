package com.connect.Auth.controller;

import com.connect.Auth.dto.AuthResponse;
import com.connect.Auth.dto.LoginRequest;
import com.connect.Auth.dto.OrganizationRegistrationRequest;
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

    @PostMapping("/register/organization")
    @Operation(summary = "Register organization with admin", description = "Creates a new organization and admin account in one step. Returns JWT token with organizationId.")
    public ResponseEntity<AuthResponse> registerOrganization(@Valid @RequestBody OrganizationRegistrationRequest request) {
        try {
            AuthResponse response = authService.registerOrganization(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            HttpStatus status = e.getMessage() != null && 
                    (e.getMessage().contains("already exists") || e.getMessage().contains("Invalid")) 
                    ? HttpStatus.BAD_REQUEST 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message("Internal server error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
    @Operation(summary = "Update admin organizationId", description = "Internal endpoint called by User Service to sync organizationId after organization creation. Returns new JWT token with updated organizationId.")
    public ResponseEntity<AuthResponse> updateAdminOrganizationId(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || request.get("adminId") == null || request.get("organizationId") == null) {
                AuthResponse errorResponse = AuthResponse.builder()
                        .message("Missing required fields: adminId and organizationId are required")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            Long adminId = Long.valueOf(request.get("adminId").toString());
            Long organizationId = Long.valueOf(request.get("organizationId").toString());
            
            AuthResponse response = authService.updateOrganizationIdAndGetToken(adminId, organizationId);
            
            if (response == null || response.getToken() == null) {
                AuthResponse errorResponse = AuthResponse.builder()
                        .message("Failed to generate token after updating organizationId")
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message("Invalid adminId or organizationId format. Both must be valid numbers.")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("not found") 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .message("Internal server error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
