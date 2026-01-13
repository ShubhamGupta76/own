package com.connect.Chat.repository;

import com.connect.Chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages in a chat room, ordered by creation time
     */
    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    
    /**
     * Find all messages in a chat room (without pagination)
     */
    List<Message> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    
    /**
     * Find messages by organization
     */
    List<Message> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    
    /**
     * Find messages by sender
     */
    List<Message> findBySenderIdAndOrganizationIdOrderByCreatedAtDesc(Long senderId, Long organizationId);
    
    /**
     * Count unread messages for a user in a chat room
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoomId = :chatRoomId " +
           "AND m.senderId != :userId AND m.status != 'READ'")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}

