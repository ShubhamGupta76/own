package com.connect.File.service;

import com.connect.File.entity.FilePolicy;
import com.connect.File.repository.FilePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilePolicyService {
    
    private final FilePolicyRepository filePolicyRepository;
    
    @Transactional(readOnly = true)
    public FilePolicy getFilePolicy(Long organizationId) {
        return filePolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> FilePolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .maxFileSizeMb(100)
                        .allowedFileTypes("pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,png")
                        .build());
    }
    
    @Transactional
    public FilePolicy updateFilePolicy(Long organizationId, Boolean enabled, Integer maxFileSizeMb, String allowedFileTypes) {
        FilePolicy policy = filePolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> FilePolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .maxFileSizeMb(100)
                        .allowedFileTypes("pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,png")
                        .build());
        
        if (enabled != null) policy.setEnabled(enabled);
        if (maxFileSizeMb != null) policy.setMaxFileSizeMb(maxFileSizeMb);
        if (allowedFileTypes != null) policy.setAllowedFileTypes(allowedFileTypes);
        
        return filePolicyRepository.save(policy);
    }
}

