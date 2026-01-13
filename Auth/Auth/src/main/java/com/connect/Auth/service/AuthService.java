package com.connect.Auth.service;

import com.connect.Auth.dto.AuthResponse;
import com.connect.Auth.dto.LoginRequest;
import com.connect.Auth.dto.RegisterRequest;
import com.connect.Auth.entity.Admin;
import com.connect.Auth.repository.AdminRepository;
import com.connect.Auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations
 * Handles admin registration and login
 * Note: Employee login is handled by EmployeeAuthService
 */
@Service
@RequiredArgsConstructor
public class AuthService {

        private final AdminRepository adminRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

        /**
         * Register a new admin
         * Auto-creates an organization with the same ID as the admin
         */
        @Transactional
        public AuthResponse register(RegisterRequest request) {
                // Check if admin already exists
                if (adminRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Admin with email " + request.getEmail() + " already exists");
                }

                // Create new admin (initially without organizationId)
                Admin admin = Admin.builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .role(Admin.Role.ADMIN)
                                .active(true)
                                .build();

                admin = adminRepository.save(admin);

                // Auto-assign organizationId (use admin ID as organization ID)
                admin.setOrganizationId(admin.getId());
                admin = adminRepository.save(admin);

                // Generate JWT token with organizationId
                String token = jwtUtil.generateToken(
                                admin.getId(),
                                admin.getEmail(),
                                admin.getRole().name(),
                                admin.getOrganizationId());

                return AuthResponse.builder()
                                .token(token)
                                .userId(admin.getId())
                                .email(admin.getEmail())
                                .role(admin.getRole().name())
                                .organizationId(admin.getOrganizationId())
                                .message("Admin registered successfully")
                                .build();
        }

        /**
         * Login admin and generate JWT token
         */
        @Transactional(readOnly = true)
        public AuthResponse login(LoginRequest request) {
                // Find admin by email
                Admin admin = adminRepository.findByEmailAndActiveTrue(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

                // Verify password
                if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                        throw new RuntimeException("Invalid email or password");
                }

                // Generate JWT token
                String token = jwtUtil.generateToken(
                                admin.getId(),
                                admin.getEmail(),
                                admin.getRole().name(),
                                admin.getOrganizationId());

                return AuthResponse.builder()
                                .token(token)
                                .userId(admin.getId())
                                .email(admin.getEmail())
                                .role(admin.getRole().name())
                                .organizationId(admin.getOrganizationId())
                                .message("Login successful")
                                .build();
        }

        /**
         * Update organizationId for admin (called after organization creation)
         */
        @Transactional
        public void updateOrganizationId(Long adminId, Long organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                adminRepository.save(admin);
        }
}
