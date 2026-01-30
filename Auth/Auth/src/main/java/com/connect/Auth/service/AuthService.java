package com.connect.Auth.service;

import com.connect.Auth.dto.AuthResponse;
import com.connect.Auth.dto.LoginRequest;
import com.connect.Auth.dto.OrganizationRegistrationRequest;
import com.connect.Auth.dto.RegisterRequest;
import com.connect.Auth.entity.Admin;
import com.connect.Auth.repository.AdminRepository;
import com.connect.Auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final AdminRepository adminRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final WebClient webClient;
        
        @Value("${user.service.url:http://localhost:8102}")
        private String userServiceUrl;

        public AuthResponse register(RegisterRequest request) {
                // Normalize email to lowercase for consistency
                String normalizedEmail = request.getEmail().toLowerCase().trim();
                
                if (adminRepository.existsByEmail(normalizedEmail)) {
                        throw new RuntimeException("Admin with email " + normalizedEmail + " already exists");
                }

                Admin admin = Admin.builder()
                                .email(normalizedEmail)
                                .password(passwordEncoder.encode(request.getPassword()))
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .role(Admin.Role.ADMIN)
                                .active(true)
                                .organizationId(null)
                                .build();

                admin = adminRepository.save(admin);

                String token = jwtUtil.generateToken(
                                admin.getId(),
                                admin.getEmail(),
                                admin.getRole().name(),
                                admin.getOrganizationId());

                return AuthResponse.builder()
                                .token(token)
                                .userId(admin.getId())
                                .email(admin.getEmail())
                                .firstName(admin.getFirstName())
                                .lastName(admin.getLastName())
                                .role(admin.getRole().name())
                                .organizationId(admin.getOrganizationId())
                                .message("Admin registered successfully")
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                // Normalize email to lowercase for case-insensitive lookup
                String normalizedEmail = request.getEmail().toLowerCase().trim();
                
                log.info("Attempting login for email: {}", normalizedEmail);
                
                // Try case-insensitive lookup
                Optional<Admin> adminOpt = adminRepository.findByEmailAndActiveTrue(normalizedEmail);
                
                // If not found, try original email (in case it was stored with different case)
                if (adminOpt.isEmpty()) {
                        adminOpt = adminRepository.findByEmailAndActiveTrue(request.getEmail().trim());
                }
                
                Admin admin = adminOpt.orElseThrow(() -> {
                        log.warn("Login failed: Admin not found with email: {} (normalized: {})", request.getEmail(), normalizedEmail);
                        return new RuntimeException("Invalid email or password");
                });

                log.info("Admin found: ID={}, Email={}, Active={}", admin.getId(), admin.getEmail(), admin.getActive());
                
                if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                        log.warn("Login failed: Password mismatch for email: {}", normalizedEmail);
                        throw new RuntimeException("Invalid email or password");
                }
                
                log.info("Password verified successfully for email: {}", normalizedEmail);

                String organizationId = admin.getOrganizationId();
                
                // Log organizationId status for debugging
                if (organizationId == null || organizationId.isEmpty()) {
                        log.warn("Admin {} logged in but has no organizationId assigned. Token will not include organizationId.", admin.getEmail());
                } else {
                        log.info("Admin {} logged in with organizationId: {}", admin.getEmail(), organizationId);
                }

                String token = jwtUtil.generateToken(
                                admin.getId(),
                                admin.getEmail(),
                                admin.getRole().name(),
                                organizationId);

                log.debug("Generated token for admin {} with organizationId: {}", admin.getEmail(), organizationId);

                return AuthResponse.builder()
                                .token(token)
                                .userId(admin.getId())
                                .email(admin.getEmail())
                                .firstName(admin.getFirstName())
                                .lastName(admin.getLastName())
                                .role(admin.getRole().name())
                                .organizationId(organizationId)
                                .message("Login successful")
                                .build();
        }

        public Admin updateAdminOrganizationId(String adminId, String organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                admin = adminRepository.save(admin);
                log.info("Updated organizationId {} for admin {} in separate transaction", organizationId, adminId);
                return admin;
        }

        public void updateOrganizationId(String adminId, String organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                adminRepository.save(admin);
        }

        public AuthResponse updateOrganizationIdAndGetToken(String adminId, String organizationId) {
                log.info("Updating organizationId {} for admin {}", organizationId, adminId);
                
                // Try to find admin, with retry logic
                Admin admin = findAdminWithRetry(adminId);
                
                admin.setOrganizationId(organizationId);
                admin = adminRepository.save(admin);
                log.info("Successfully updated organizationId {} for admin {}", organizationId, adminId);

                String token = jwtUtil.generateToken(
                                admin.getId(),
                                admin.getEmail(),
                                admin.getRole().name(),
                                admin.getOrganizationId());

                return AuthResponse.builder()
                                .token(token)
                                .userId(admin.getId())
                                .email(admin.getEmail())
                                .firstName(admin.getFirstName())
                                .lastName(admin.getLastName())
                                .role(admin.getRole().name())
                                .organizationId(admin.getOrganizationId())
                                .message("Organization ID updated successfully")
                                .build();
        }

        public AuthResponse registerOrganization(OrganizationRegistrationRequest request) {
                // First, register the admin user
                Admin admin = createAdminUser(request);
                final String adminId = admin.getId();
                
                // Verify admin exists in database
                Admin verifiedAdmin = verifyAdminExists(adminId);
                final String adminEmail = verifiedAdmin.getEmail();
                log.info("Admin verified and committed successfully with ID: {}", adminId);

                // Generate temporary token for calling User service
                String tempToken = jwtUtil.generateToken(
                                adminId,
                                adminEmail,
                                admin.getRole().name(),
                                null);

                // Call User service to create organization
                try {
                        Map<String, Object> orgRequest = new HashMap<>();
                        orgRequest.put("name", request.getOrganizationName());
                        orgRequest.put("domain", null); // Optional, can be extracted from email if needed

                        log.info("Calling User service to create organization: {} for admin: {}", 
                                request.getOrganizationName(), adminId);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> orgResponse = webClient.post()
                                        .uri(userServiceUrl + "/api/v1/organizations")
                                        .header("Authorization", "Bearer " + tempToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(orgRequest)
                                        .retrieve()
                                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                                clientResponse -> {
                                                        log.error("User service returned error status: {} for admin: {}",
                                                                clientResponse.statusCode(), adminId);
                                                        return clientResponse.bodyToMono(String.class)
                                                                .flatMap(body -> {
                                                                        String errorMsg = "User service error: " + clientResponse.statusCode() + " - " + body;
                                                                        log.error("User service error details: {}", errorMsg);
                                                                        return Mono.error(new RuntimeException(errorMsg));
                                                                });
                                                })
                                        .bodyToMono(Map.class)
                                        .block();

                        if (orgResponse == null) {
                                log.error("User service returned null response for admin: {}", adminId);
                                throw new RuntimeException("User service returned null response. Please check that User service is running.");
                        }

                        // Extract organizationId and new token from response
                        String organizationId = null;
                        String newToken = null;
                        
                        if (orgResponse.get("organizationId") != null) {
                                organizationId = orgResponse.get("organizationId").toString();
                        } else if (orgResponse.get("organization") != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> org = (Map<String, Object>) orgResponse.get("organization");
                                if (org.get("id") != null) {
                                        organizationId = org.get("id").toString();
                                }
                        }

                        if (orgResponse.get("token") != null) {
                                newToken = orgResponse.get("token").toString();
                        }

                        if (organizationId == null) {
                                log.error("User service did not return organizationId for admin: {}", adminId);
                                throw new RuntimeException("Failed to create organization: organizationId not returned");
                        }

                        // Update admin's organizationId in a separate transaction
                        Admin updatedAdmin = updateAdminOrganizationId(adminId, organizationId);

                        // Always generate a fresh token with the updated organizationId to ensure it's in the token
                        // The token from User service might have been generated before organizationId was set
                        String finalToken = jwtUtil.generateToken(
                                        updatedAdmin.getId(),
                                        updatedAdmin.getEmail(),
                                        updatedAdmin.getRole().name(),
                                        updatedAdmin.getOrganizationId());
                        
                        log.info("Generated final token with organizationId: {} for admin: {}", 
                                updatedAdmin.getOrganizationId(), adminId);

                        log.info("Organization created successfully with ID: {} for admin: {}", organizationId, adminId);

                        return AuthResponse.builder()
                                        .token(finalToken)
                                        .userId(updatedAdmin.getId())
                                        .email(updatedAdmin.getEmail())
                                        .firstName(updatedAdmin.getFirstName())
                                        .lastName(updatedAdmin.getLastName())
                                        .role(updatedAdmin.getRole().name())
                                        .organizationId(organizationId)
                                        .message("Organization and admin account created successfully")
                                        .build();

                } catch (org.springframework.web.reactive.function.client.WebClientException e) {
                        log.error("WebClient error creating organization for admin {}: {}", adminId, e.getMessage(), e);
                        String errorMsg = "Cannot connect to User service. Please ensure User service is running at " + userServiceUrl;
                        if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                                errorMsg = "User service is not running or not accessible at " + userServiceUrl + ". Please start the User service.";
                        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                                errorMsg = "User service request timed out. Please check network connectivity and User service status.";
                        }
                        throw new RuntimeException(errorMsg);
                } catch (RuntimeException e) {
                        // Re-throw RuntimeExceptions as-is (they already have proper messages)
                        throw e;
                } catch (Exception e) {
                        log.error("Unexpected error creating organization for admin {}: {}", adminId, e.getMessage(), e);
                        throw new RuntimeException("Failed to create organization: " +
                                        (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                }
        }

        private Admin createAdminUser(OrganizationRegistrationRequest request) {
                // Normalize email to lowercase for consistency
                String normalizedEmail = request.getAdminEmail().toLowerCase().trim();
                
                if (adminRepository.existsByEmail(normalizedEmail)) {
                        throw new RuntimeException("Admin with email " + normalizedEmail + " already exists");
                }

                Admin admin = Admin.builder()
                                .email(normalizedEmail)
                                .password(passwordEncoder.encode(request.getAdminPassword()))
                                .firstName(request.getAdminFirstName())
                                .lastName(request.getAdminLastName())
                                .role(Admin.Role.ADMIN)
                                .active(true)
                                .organizationId(null)
                                .build();

                admin = adminRepository.save(admin);
                log.info("Admin created with ID: {}", admin.getId());
                return admin;
        }

        /**
         * Verify admin exists in database
         */
        private Admin verifyAdminExists(String adminId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Admin with ID " + adminId + " not found after creation."));
                
                log.info("Admin verified successfully: ID={}, Email={}", admin.getId(), admin.getEmail());
                return admin;
        }

        /**
         * Find admin with retry logic
         */
        private Admin findAdminWithRetry(String adminId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));
                return admin;
        }
}
