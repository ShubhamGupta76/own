package com.connect.Task.repository;

import com.connect.Task.entity.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    
    List<Task> findByOrganizationId(String organizationId);
    
    List<Task> findByChannelIdAndOrganizationId(String channelId, String organizationId);
    
    List<Task> findByTeamIdAndOrganizationId(String teamId, String organizationId);
    
    List<Task> findByAssignedToAndOrganizationId(String assignedTo, String organizationId);
    
    Optional<Task> findByIdAndOrganizationId(String id, String organizationId);
    
    List<Task> findByStatusAndOrganizationId(Task.TaskStatus status, String organizationId);
}

