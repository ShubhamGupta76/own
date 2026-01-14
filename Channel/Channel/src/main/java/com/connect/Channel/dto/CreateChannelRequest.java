package com.connect.Channel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {
    
    @NotBlank(message = "Channel name is required")
    private String name;
    
    private String description;
    
    private String type = "STANDARD"; 
    
    private Boolean chatEnabled = true;
    private Boolean fileEnabled = true;
    private Boolean meetingEnabled = true;
}

