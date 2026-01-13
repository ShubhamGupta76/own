package com.connect.File.service;

import com.connect.File.dto.FileMetadataResponse;
import com.connect.File.dto.FileUploadResponse;
import com.connect.File.entity.FileMetadata;
import com.connect.File.entity.FilePolicy;
import com.connect.File.repository.FileMetadataRepository;
import com.connect.File.repository.FilePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for file operations
 * Handles file upload, download, versioning, and locking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileMetadataRepository fileMetadataRepository;
    private final FilePolicyRepository filePolicyRepository;
    private final FileEventProducer eventProducer;
    
    @Value("${file.storage.path:./files}")
    private String storagePath;
    
    /**
     * Upload a file
     * Stores file on filesystem and metadata in database
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, Long channelId, Long chatMessageId, 
                                       Long uploadedBy, Long organizationId, String role) {
        // Validate file policy
        validateFilePolicy(organizationId, file, role);
        
        // Validate permissions
        if (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("EMPLOYEE")) {
            throw new RuntimeException("Access denied: Insufficient permissions");
        }
        
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(storagePath);
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + e.getMessage());
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = storageDir.resolve(uniqueFilename);
        
        // Save file to filesystem
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
        
        // Save metadata to database
        FileMetadata fileMetadata = FileMetadata.builder()
                .filename(originalFilename)
                .size(file.getSize())
                .contentType(file.getContentType())
                .filePath(filePath.toString())
                .channelId(channelId)
                .chatMessageId(chatMessageId)
                .uploadedBy(uploadedBy)
                .organizationId(organizationId)
                .version(1)
                .build();
        
        fileMetadata = fileMetadataRepository.save(fileMetadata);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishFileUploadedEvent(
                fileMetadata.getId(),
                fileMetadata.getFilename(),
                channelId,
                uploadedBy,
                organizationId,
                fileMetadata.getSize(),
                fileMetadata.getContentType()
        );
        
        return mapToUploadResponse(fileMetadata);
    }
    
    /**
     * Get files for a channel
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getChannelFiles(Long channelId, Long organizationId) {
        List<FileMetadata> files = fileMetadataRepository.findByChannelIdAndOrganizationId(channelId, organizationId);
        
        return files.stream()
                .map(this::mapToMetadataResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get file metadata by ID
     */
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(Long fileId, Long organizationId) {
        FileMetadata file = fileMetadataRepository.findByIdAndOrganizationId(fileId, organizationId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        return mapToMetadataResponse(file);
    }
    
    /**
     * Download file
     * Returns Resource for file download
     */
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, Long organizationId) {
        FileMetadata file = fileMetadataRepository.findByIdAndOrganizationId(fileId, organizationId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        try {
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file: " + e.getMessage());
        }
    }
    
    /**
     * Lock a file for editing
     */
    @Transactional
    public FileMetadataResponse lockFile(Long fileId, Long userId, Long organizationId) {
        FileMetadata file = fileMetadataRepository.findByIdAndOrganizationId(fileId, organizationId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Check if file is already locked
        if (file.getLockedBy() != null && !file.getLockedBy().equals(userId)) {
            throw new RuntimeException("File is already locked by another user");
        }
        
        // Lock the file
        file.setLockedBy(userId);
        file.setLockedAt(LocalDateTime.now());
        file = fileMetadataRepository.save(file);
        
        return mapToMetadataResponse(file);
    }
    
    /**
     * Unlock a file
     */
    @Transactional
    public FileMetadataResponse unlockFile(Long fileId, Long userId, Long organizationId) {
        FileMetadata file = fileMetadataRepository.findByIdAndOrganizationId(fileId, organizationId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Check if file is locked by this user
        if (file.getLockedBy() == null) {
            throw new RuntimeException("File is not locked");
        }
        
        if (!file.getLockedBy().equals(userId)) {
            throw new RuntimeException("You can only unlock files that you locked");
        }
        
        // Unlock the file
        file.setLockedBy(null);
        file.setLockedAt(null);
        file = fileMetadataRepository.save(file);
        
        return mapToMetadataResponse(file);
    }
    
    /**
     * Create new version of a file
     * Increments version number
     */
    @Transactional
    public FileUploadResponse createNewVersion(MultipartFile file, Long existingFileId, 
                                              Long uploadedBy, Long organizationId, String role) {
        // Get existing file
        FileMetadata existingFile = fileMetadataRepository.findByIdAndOrganizationId(existingFileId, organizationId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Validate file policy
        validateFilePolicy(organizationId, file, role);
        
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(storagePath);
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + e.getMessage());
        }
        
        // Generate unique filename for new version
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = storageDir.resolve(uniqueFilename);
        
        // Save file to filesystem
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
        
        // Create new version metadata
        FileMetadata newVersion = FileMetadata.builder()
                .filename(originalFilename)
                .size(file.getSize())
                .contentType(file.getContentType())
                .filePath(filePath.toString())
                .channelId(existingFile.getChannelId())
                .chatMessageId(existingFile.getChatMessageId())
                .uploadedBy(uploadedBy)
                .organizationId(organizationId)
                .version(existingFile.getVersion() + 1) // Increment version
                .build();
        
        newVersion = fileMetadataRepository.save(newVersion);
        
        return mapToUploadResponse(newVersion);
    }
    
    /**
     * Validate file policy
     */
    private void validateFilePolicy(Long organizationId, MultipartFile file, String role) {
        FilePolicy policy = filePolicyRepository.findByOrganizationId(organizationId)
                .orElse(FilePolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .build());
        
        if (!policy.getEnabled()) {
            throw new RuntimeException("File sharing is disabled for your organization");
        }
        
        // Check file size
        if (policy.getMaxFileSizeMb() != null) {
            long maxSizeBytes = policy.getMaxFileSizeMb() * 1024L * 1024L;
            if (file.getSize() > maxSizeBytes) {
                throw new RuntimeException("File size exceeds maximum allowed size: " + policy.getMaxFileSizeMb() + " MB");
            }
        }
        
        // Check file type
        if (policy.getAllowedFileTypes() != null && !policy.getAllowedFileTypes().isEmpty()) {
            String contentType = file.getContentType();
            String[] allowedTypes = policy.getAllowedFileTypes().split(",");
            boolean isAllowed = false;
            for (String allowedType : allowedTypes) {
                if (contentType != null && contentType.contains(allowedType.trim())) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new RuntimeException("File type not allowed");
            }
        }
    }
    
    /**
     * Map FileMetadata to FileUploadResponse
     */
    private FileUploadResponse mapToUploadResponse(FileMetadata file) {
        return FileUploadResponse.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .channelId(file.getChannelId())
                .chatMessageId(file.getChatMessageId())
                .uploadedBy(file.getUploadedBy())
                .organizationId(file.getOrganizationId())
                .version(file.getVersion())
                .uploadedAt(file.getUploadedAt())
                .downloadUrl("/api/files/" + file.getId() + "/download")
                .build();
    }
    
    /**
     * Map FileMetadata to FileMetadataResponse
     */
    private FileMetadataResponse mapToMetadataResponse(FileMetadata file) {
        return FileMetadataResponse.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .channelId(file.getChannelId())
                .chatMessageId(file.getChatMessageId())
                .uploadedBy(file.getUploadedBy())
                .organizationId(file.getOrganizationId())
                .version(file.getVersion())
                .lockedBy(file.getLockedBy())
                .lockedAt(file.getLockedAt())
                .isLocked(file.getLockedBy() != null)
                .uploadedAt(file.getUploadedAt())
                .updatedAt(file.getUpdatedAt())
                .downloadUrl("/api/files/" + file.getId() + "/download")
                .build();
    }
}

