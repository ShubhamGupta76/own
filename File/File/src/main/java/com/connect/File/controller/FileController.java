package com.connect.File.controller;

import com.connect.File.dto.FileMetadataResponse;
import com.connect.File.dto.FileUploadResponse;
import com.connect.File.service.FileService;
import com.connect.File.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for file operations
 * Handles file upload, download, lock/unlock, and versioning
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "File sharing and collaboration APIs")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private String getRole(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractRole(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Upload a file
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Upload file", description = "Uploads a file to a channel or attaches it to a chat message. Supports file versioning.")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) Long chatMessageId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            // Validate that at least one of channelId or chatMessageId is provided
            if (channelId == null && chatMessageId == null) {
                throw new RuntimeException("Either channelId or chatMessageId must be provided");
            }
            
            FileUploadResponse response = fileService.uploadFile(file, channelId, chatMessageId, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get files for a channel
     * GET /api/files/channel/{channelId}
     */
    @GetMapping("/channel/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get channel files", description = "Retrieves all files uploaded to a channel.")
    public ResponseEntity<List<FileMetadataResponse>> getChannelFiles(
            @PathVariable Long channelId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<FileMetadataResponse> files = fileService.getChannelFiles(channelId, organizationId);
            return ResponseEntity.ok(files);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Download a file
     * GET /api/files/{fileId}/download
     */
    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Download file", description = "Downloads a file by ID.")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            Resource resource = fileService.downloadFile(fileId, organizationId);
            FileMetadataResponse metadata = fileService.getFileMetadata(fileId, organizationId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Lock a file for editing
     * POST /api/files/{fileId}/lock
     */
    @PostMapping("/{fileId}/lock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Lock file", description = "Locks a file for editing. Only the user who locked it can unlock it.")
    public ResponseEntity<FileMetadataResponse> lockFile(
            @PathVariable Long fileId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            FileMetadataResponse response = fileService.lockFile(fileId, userId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Unlock a file
     * POST /api/files/{fileId}/unlock
     */
    @PostMapping("/{fileId}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Unlock file", description = "Unlocks a file. Only the user who locked it can unlock it.")
    public ResponseEntity<FileMetadataResponse> unlockFile(
            @PathVariable Long fileId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            FileMetadataResponse response = fileService.unlockFile(fileId, userId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get file metadata
     * GET /api/files/{fileId}
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get file metadata", description = "Retrieves file metadata including lock status and version.")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(
            @PathVariable Long fileId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            FileMetadataResponse response = fileService.getFileMetadata(fileId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

