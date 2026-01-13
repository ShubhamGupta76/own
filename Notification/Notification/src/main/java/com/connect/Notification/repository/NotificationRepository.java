package com.connect.Notification.repository;

import com.connect.Notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByOrganizationId(Long organizationId);
    List<Notification> findByOrganizationIdAndEnabledTrue(Long organizationId);
    
    List<Notification> findByUserIdAndOrganizationIdOrderByCreatedAtDesc(Long userId, Long organizationId);
    
    List<Notification> findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(Long userId, Long organizationId);
    
    Long countByUserIdAndReadFalseAndOrganizationId(Long userId, Long organizationId);
}

