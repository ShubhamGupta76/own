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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                if (adminRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Admin with email " + request.getEmail() + " already exists");
                }

                Admin admin = Admin.builder()
                                .email(request.getEmail())
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

        @Transactional(readOnly = true)
        public AuthResponse login(LoginRequest request) {
                Admin admin = adminRepository.findByEmailAndActiveTrue(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

                if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                        throw new RuntimeException("Invalid email or password");
                }

                Long organizationId = admin.getOrganizationId();
                
                // Log organizationId status for debugging
                if (organizationId == null || organizationId == 0) {
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

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public Admin updateAdminOrganizationId(Long adminId, Long organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                admin = adminRepository.save(admin);
                log.info("Updated organizationId {} for admin {} in separate transaction", organizationId, adminId);
                return admin;
        }

        @Transactional
        public void updateOrganizationId(Long adminId, Long organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                adminRepository.save(admin);
        }

        @Transactional
        public AuthResponse updateOrganizationIdAndGetToken(Long adminId, Long organizationId) {
                log.info("Updating organizationId {} for admin {}", organizationId, adminId);
                
                // Try to find admin, with retry logic for transaction visibility
                Admin admin = findAdminWithRetry(adminId);
                
                admin.setOrganizationId(organizationId);
                admin = adminRepository.save(admin);
                // Transaction will commit automatically when method returns
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
                // First, register the admin user in a separate transaction to ensure it's committed
                Admin admin = createAdminUser(request);
                final Long adminId = admin.getId();
                
                // Verify admin exists in database (this ensures the transaction is committed and visible)
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
                        Long organizationId = null;
                        String newToken = null;
                        
                        if (orgResponse.get("organizationId") != null) {
                                organizationId = Long.valueOf(orgResponse.get("organizationId").toString());
                        } else if (orgResponse.get("organization") != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> org = (Map<String, Object>) orgResponse.get("organization");
                                if (org.get("id") != null) {
                                        organizationId = Long.valueOf(org.get("id").toString());
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

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        private Admin createAdminUser(OrganizationRegistrationRequest request) {
                if (adminRepository.existsByEmail(request.getAdminEmail())) {
                        throw new RuntimeException("Admin with email " + request.getAdminEmail() + " already exists");
                }

                Admin admin = Admin.builder()
                                .email(request.getAdminEmail())
                                .password(passwordEncoder.encode(request.getAdminPassword()))
                                .firstName(request.getAdminFirstName())
                                .lastName(request.getAdminLastName())
                                .role(Admin.Role.ADMIN)
                                .active(true)
                                .organizationId(null)
                                .build();

                admin = adminRepository.save(admin);
                // Transaction will commit automatically when method returns due to REQUIRES_NEW
                log.info("Admin created and will be committed with ID: {} in separate transaction", admin.getId());
                return admin;
        }

        /**
         * Verify admin exists in database - this ensures the transaction is committed and visible
         * Uses a new transaction to read from database, ensuring we see committed data
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
        private Admin verifyAdminExists(Long adminId) {
                // Small delay to ensure transaction propagation (only if needed)
                try {
                        Thread.sleep(50); // 50ms delay to ensure transaction is fully committed
                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Thread interrupted during admin verification delay");
                }
                
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Admin with ID " + adminId + " not found after creation. " +
                                        "This may indicate a transaction commit issue."));
                
                log.info("Admin verified successfully: ID={}, Email={}", admin.getId(), admin.getEmail());
                return admin;
        }

        /**
         * Find admin with retry logic to handle transaction visibility issues
         */
        private Admin findAdminWithRetry(Long adminId) {
                int maxRetries = 3;
                int retryDelay = 100; // milliseconds
                
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        Admin admin = adminRepository.findById(adminId).orElse(null);
                        if (admin != null) {
                                if (attempt > 1) {
                                        log.info("Admin found on attempt {} for adminId {}", attempt, adminId);
                                }
                                return admin;
                        }
                        
                        if (attempt < maxRetries) {
                                log.warn("Admin not found on attempt {} for adminId {}, retrying in {}ms", 
                                        attempt, adminId, retryDelay);
                                try {
                                        Thread.sleep(retryDelay);
                                } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                }
                                // Exponential backoff
                                retryDelay *= 2;
                        }
                }
                
                log.error("Admin with ID {} not found after {} attempts", adminId, maxRetries);
                throw new RuntimeException("Admin not found with ID: " + adminId + 
                        ". The admin may not have been committed yet or may not exist.");
        }
}
