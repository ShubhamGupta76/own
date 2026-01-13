package com.connect.Search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String type; // USER, TEAM, CHANNEL
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
}

