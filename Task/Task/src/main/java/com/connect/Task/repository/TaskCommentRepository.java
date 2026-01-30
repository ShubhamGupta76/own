package com.connect.Task.repository;

import com.connect.Task.entity.TaskComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskComment entity
 */
@Repository
public interface TaskCommentRepository extends MongoRepository<TaskComment, String> {
    
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(String taskId);
    
    List<TaskComment> findByTaskIdAndOrganizationId(String taskId, String organizationId);
}

