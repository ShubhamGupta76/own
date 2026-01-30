package com.connect.Chat.repository;

import com.connect.Chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    
    /**
     * Find all messages in a chat room, ordered by creation time
     */
    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(String chatRoomId, Pageable pageable);
    
    /**
     * Find all messages in a chat room (without pagination)
     */
    List<Message> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);
    
    /**
     * Find messages by organization
     */
    List<Message> findByOrganizationIdOrderByCreatedAtDesc(String organizationId);
    
    /**
     * Find messages by sender
     */
    List<Message> findBySenderIdAndOrganizationIdOrderByCreatedAtDesc(String senderId, String organizationId);
    
    /**
     * Count unread messages for a user in a chat room
     */
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'status': { $ne: 'READ' } }")
    long countByChatRoomIdAndSenderIdNotAndStatusNot(String chatRoomId, String userId, Message.MessageStatus status);
    
    default Long countUnreadMessages(String chatRoomId, String userId) {
        return countByChatRoomIdAndSenderIdNotAndStatusNot(chatRoomId, userId, Message.MessageStatus.READ);
    }
}

