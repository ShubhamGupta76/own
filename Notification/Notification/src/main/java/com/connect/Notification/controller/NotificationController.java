package com.connect.Notification.controller;

import com.connect.Notification.dto.NotificationResponse;
import com.connect.Notification.entity.NotificationConfig;
import com.connect.Notification.service.NotificationService;
import com.connect.Notification.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for notification operations
 * Handles notifications, activity feed, and real-time delivery
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "Notification and activity feed APIs")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
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
     * Get notifications for logged-in user
     * GET /api/notifications
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get notifications", description = "Retrieves all notifications for the logged-in user.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<NotificationResponse> notifications = notificationService.getNotifications(userId, organizationId);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get unread notifications", description = "Retrieves unread notifications for the logged-in user.")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, organizationId);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get unread notification count
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get unread count", description = "Returns the count of unread notifications.")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            Long count = notificationService.getUnreadCount(userId, organizationId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Mark notification as read
     * POST /api/notifications/{id}/read
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read.")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            NotificationResponse notification = notificationService.markAsRead(id, userId, organizationId);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Mark all notifications as read
     * POST /api/notifications/read-all
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Mark all as read", description = "Marks all notifications as read for the logged-in user.")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            notificationService.markAllAsRead(userId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get activity feed
     * GET /api/notifications/activity
     */
    @GetMapping("/activity")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get activity feed", description = "Retrieves activity feed for the logged-in user.")
    public ResponseEntity<List<NotificationResponse>> getActivityFeed(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<NotificationResponse> activities = notificationService.getActivityFeed(userId, organizationId, limit);
            return ResponseEntity.ok(activities);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get notification configs (admin only)
     * GET /api/notifications/configs
     */
    @GetMapping("/configs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification configs", description = "Retrieves notification type configurations.")
    public ResponseEntity<List<NotificationConfig>> getNotificationConfigs(HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(notificationService.getNotificationConfigs(organizationId));
    }
    
    /**
     * Update notification config (admin only)
     * PUT /api/notifications/configs/{type}
     */
    @PutMapping("/configs/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update notification config", description = "Enables or disables a specific notification type.")
    public ResponseEntity<NotificationConfig> updateNotificationConfig(
            @PathVariable String type,
            @RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        
        com.connect.Notification.entity.Notification.NotificationType notificationType;
        try {
            notificationType = com.connect.Notification.entity.Notification.NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid notification type: " + type);
        }
        
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            throw new RuntimeException("Enabled status is required");
        }
        
        return ResponseEntity.ok(notificationService.updateNotificationConfig(organizationId, notificationType, enabled));
    }
}
