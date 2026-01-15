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

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AdminRepository adminRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

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

        @Transactional
        public void updateOrganizationId(Long adminId, Long organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
                adminRepository.save(admin);
        }

        @Transactional
        public AuthResponse updateOrganizationIdAndGetToken(Long adminId, Long organizationId) {
                Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin not found"));
                admin.setOrganizationId(organizationId);
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
                                .role(admin.getRole().name())
                                .organizationId(admin.getOrganizationId())
                                .message("Organization ID updated successfully")
                                .build();
        }
}
