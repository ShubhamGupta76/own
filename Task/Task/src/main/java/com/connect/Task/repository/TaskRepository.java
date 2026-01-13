package com.connect.Task.repository;

import com.connect.Task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByOrganizationId(Long organizationId);
    
    List<Task> findByChannelIdAndOrganizationId(Long channelId, Long organizationId);
    
    List<Task> findByTeamIdAndOrganizationId(Long teamId, Long organizationId);
    
    List<Task> findByAssignedToAndOrganizationId(Long assignedTo, Long organizationId);
    
    Optional<Task> findByIdAndOrganizationId(Long id, Long organizationId);
    
    List<Task> findByStatusAndOrganizationId(Task.TaskStatus status, Long organizationId);
}

