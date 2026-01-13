package com.connect.Task.service;

import com.connect.Task.dto.*;
import com.connect.Task.entity.Task;
import com.connect.Task.entity.TaskComment;
import com.connect.Task.repository.TaskCommentRepository;
import com.connect.Task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for task management operations
 * Handles task creation, assignment, status updates, and comments
 */
@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskEventProducer eventProducer;
    
    /**
     * Create a new task
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long createdBy, Long organizationId, String role) {
        // Validate role permissions
        if (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("EMPLOYEE")) {
            throw new RuntimeException("Access denied: Insufficient permissions");
        }
        
        // Validate task type
        Task.TaskType taskType;
        try {
            taskType = Task.TaskType.valueOf(request.getTaskType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task type: " + request.getTaskType());
        }
        
        // Validate priority
        Task.Priority priority;
        try {
            priority = Task.Priority.valueOf(request.getPriority().toUpperCase());
        } catch (IllegalArgumentException e) {
            priority = Task.Priority.MEDIUM;
        }
        
        // Create task
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .organizationId(organizationId)
                .teamId(request.getTeamId())
                .channelId(request.getChannelId())
                .createdBy(createdBy)
                .assignedTo(request.getAssignedTo())
                .taskType(taskType)
                .status(Task.TaskStatus.TODO)
                .priority(priority)
                .dueDate(request.getDueDate())
                .build();
        
        task = taskRepository.save(task);
        
        return mapToTaskResponse(task);
    }
    
    /**
     * Assign a task to a user
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, Long assignedToUserId, Long organizationId, String role, Long assignedByUserId) {
        // Validate role permissions (ADMIN and MANAGER can assign)
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can assign tasks");
        }
        
        // Get task
        Task task = taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Assign task
        task.setAssignedTo(assignedToUserId);
        task = taskRepository.save(task);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishTaskAssignedEvent(
                task.getId(),
                task.getTitle(),
                assignedToUserId,
                assignedByUserId,
                organizationId,
                task.getChannelId(),
                task.getTeamId()
        );
        
        return mapToTaskResponse(task);
    }
    
    /**
     * Update task status
     */
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, String status, Long userId, Long organizationId, String role) {
        // Get task
        Task task = taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Validate permissions
        // EMPLOYEE can only update if assigned to them
        if (role.equals("EMPLOYEE")) {
            if (task.getAssignedTo() == null || !task.getAssignedTo().equals(userId)) {
                throw new RuntimeException("Access denied: You can only update tasks assigned to you");
            }
        } else if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Access denied: Insufficient permissions");
        }
        
        // Validate status
        Task.TaskStatus taskStatus;
        try {
            taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task status: " + status);
        }
        
        // Store old status for event
        String oldStatus = task.getStatus().name();
        
        // Update status
        task.setStatus(taskStatus);
        task = taskRepository.save(task);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishTaskStatusChangedEvent(
                task.getId(),
                task.getTitle(),
                oldStatus,
                taskStatus.name(),
                organizationId,
                task.getChannelId(),
                task.getTeamId()
        );
        
        return mapToTaskResponse(task);
    }
    
    /**
     * Add a comment to a task
     */
    @Transactional
    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, Long userId, Long organizationId) {
        // Verify task exists
        Task task = taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Create comment
        TaskComment comment = TaskComment.builder()
                .taskId(taskId)
                .createdBy(userId)
                .organizationId(organizationId)
                .content(request.getContent())
                .build();
        
        comment = commentRepository.save(comment);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishTaskCommentedEvent(
                taskId,
                task.getTitle(),
                userId,
                organizationId,
                task.getChannelId(),
                task.getTeamId()
        );
        
        return mapToCommentResponse(comment);
    }
    
    /**
     * Get tasks for a channel
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getChannelTasks(Long channelId, Long organizationId) {
        List<Task> tasks = taskRepository.findByChannelIdAndOrganizationId(channelId, organizationId);
        
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, Long organizationId) {
        Task task = taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        return mapToTaskResponse(task);
    }
    
    /**
     * Get all tasks for organization
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(Long organizationId) {
        List<Task> tasks = taskRepository.findByOrganizationId(organizationId);
        
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks assigned to a user
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(Long userId, Long organizationId) {
        List<Task> tasks = taskRepository.findByAssignedToAndOrganizationId(userId, organizationId);
        
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Task entity to TaskResponse DTO
     */
    private TaskResponse mapToTaskResponse(Task task) {
        List<TaskComment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(task.getId());
        List<TaskCommentResponse> commentResponses = comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
        
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .organizationId(task.getOrganizationId())
                .teamId(task.getTeamId())
                .channelId(task.getChannelId())
                .createdBy(task.getCreatedBy())
                .assignedTo(task.getAssignedTo())
                .taskType(task.getTaskType().name())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }
    
    /**
     * Map TaskComment entity to TaskCommentResponse DTO
     */
    private TaskCommentResponse mapToCommentResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .createdBy(comment.getCreatedBy())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

