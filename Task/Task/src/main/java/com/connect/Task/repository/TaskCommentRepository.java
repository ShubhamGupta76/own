package com.connect.Task.repository;

import com.connect.Task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskComment entity
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<TaskComment> findByTaskIdAndOrganizationId(Long taskId, Long organizationId);
}

