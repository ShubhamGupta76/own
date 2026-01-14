package com.connect.Auth.service;

import com.connect.Auth.entity.Admin;
import com.connect.Auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {
    
    private final AdminRepository adminRepository;
    private final WebClient webClient;
    
    @Value("${user.service.url:http://localhost:8102}")
    private String userServiceUrl;
    
    @Transactional
    public Map<String, Object> migrateOrganizationIds() {
        Map<String, Object> result = new HashMap<>();
        int updated = 0;
        int notFound = 0;
        int errors = 0;
        
        try {
            List<Admin> adminsWithNullOrgId = adminRepository.findByOrganizationIdIsNull();
            log.info("Found {} admins with null organizationId", adminsWithNullOrgId.size());
            
            for (Admin admin : adminsWithNullOrgId) {
                try {
                    Map<String, Object> organization = getOrganizationByAdminId(admin.getId());
                    
                    if (organization != null && organization.get("id") != null) {
                        Long organizationId = Long.valueOf(organization.get("id").toString());
                        admin.setOrganizationId(organizationId);
                        adminRepository.save(admin);
                        updated++;
                        log.info("Updated admin {} with organizationId {}", admin.getId(), organizationId);
                    } else {
                        notFound++;
                        log.warn("Organization not found for admin {}", admin.getId());
                    }
                } catch (Exception e) {
                    errors++;
                    log.error("Error migrating organizationId for admin {}: {}", admin.getId(), e.getMessage());
                }
            }
            
            result.put("totalAdminsWithNullOrgId", adminsWithNullOrgId.size());
            result.put("updated", updated);
            result.put("notFound", notFound);
            result.put("errors", errors);
            result.put("message", "Migration completed");
            
        } catch (Exception e) {
            log.error("Error during migration: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private Map<String, Object> getOrganizationByAdminId(Long adminId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = webClient.get()
                    .uri(userServiceUrl + "/api/organizations/admin/" + adminId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result;
        } catch (Exception e) {
            log.error("Error fetching organization for admin {}: {}", adminId, e.getMessage());
            return null;
        }
    }
}

