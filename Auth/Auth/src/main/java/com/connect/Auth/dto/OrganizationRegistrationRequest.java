package com.connect.Auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRegistrationRequest {
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    private String description;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String adminPassword;

    @NotBlank(message = "First name is required")
    private String adminFirstName;

    @NotBlank(message = "Last name is required")
    private String adminLastName;
}