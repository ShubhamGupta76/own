package com.connect.User.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for inviting external users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteExternalUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private List<Long> teamIds; // Teams to grant access to
    
    private List<Long> channelIds; // Channels to grant access to
}

