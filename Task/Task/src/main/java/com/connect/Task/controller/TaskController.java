package com.connect.Task.controller;

import com.connect.Task.dto.*;
import com.connect.Task.service.TaskService;
import com.connect.Task.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for task management operations
 * Handles task creation, assignment, status updates, and comments
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Task and work management APIs (Jira-like)")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    
    private final TaskService taskService;
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
     * Create a new task
     * POST /api/tasks
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Create task", description = "Creates a new task. All roles can create tasks.")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TaskResponse task = taskService.createTask(request, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Assign a task to a user
     * PUT /api/tasks/{id}/assign
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Assign task", description = "Assigns a task to a user. Only ADMIN and MANAGER can assign tasks.")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request,
            HttpServletRequest httpRequest) {
        try {
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            Long userId = request.get("userId");
            
            if (userId == null) {
                throw new RuntimeException("User ID is required");
            }
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            Long assignedByUserId = getUserId(httpRequest);
            TaskResponse task = taskService.assignTask(id, userId, organizationId, role, assignedByUserId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Update task status
     * PUT /api/tasks/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Update task status", description = "Updates task status. ADMIN and MANAGER can update any task. EMPLOYEE can only update tasks assigned to them.")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            String status = request.get("status");
            
            if (status == null) {
                throw new RuntimeException("Status is required");
            }
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TaskResponse task = taskService.updateTaskStatus(id, status, userId, organizationId, role);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Add a comment to a task
     * POST /api/tasks/{id}/comments
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Add task comment", description = "Adds a comment to a task. All roles can comment.")
    public ResponseEntity<TaskCommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody TaskCommentRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TaskCommentResponse comment = taskService.addComment(id, request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get tasks for a channel
     * GET /api/tasks/channel/{channelId}
     */
    @GetMapping("/channel/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get channel tasks", description = "Retrieves all tasks for a channel.")
    public ResponseEntity<List<TaskResponse>> getChannelTasks(
            @PathVariable Long channelId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<TaskResponse> tasks = taskService.getChannelTasks(channelId, organizationId);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get task by ID
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get task", description = "Retrieves task details with comments.")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TaskResponse task = taskService.getTask(id, organizationId);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get all tasks
     * GET /api/tasks
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks in the organization.")
    public ResponseEntity<List<TaskResponse>> getAllTasks(HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<TaskResponse> tasks = taskService.getAllTasks(organizationId);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get my tasks
     * GET /api/tasks/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get my tasks", description = "Retrieves tasks assigned to the logged-in user.")
    public ResponseEntity<List<TaskResponse>> getMyTasks(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<TaskResponse> tasks = taskService.getMyTasks(userId, organizationId);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

