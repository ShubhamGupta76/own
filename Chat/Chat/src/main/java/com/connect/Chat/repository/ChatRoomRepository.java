package com.connect.Chat.repository;

import com.connect.Chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ChatRoom entity
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    /**
     * Find chat room by type and room ID
     */
    Optional<ChatRoom> findByRoomTypeAndRoomIdAndOrganizationId(
            ChatRoom.RoomType roomType, 
            Long roomId, 
            Long organizationId
    );
    
    /**
     * Find direct chat room between two users
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'DIRECT' " +
           "AND cr.organizationId = :organizationId " +
           "AND ((cr.user1Id = :user1Id AND cr.user2Id = :user2Id) " +
           "OR (cr.user1Id = :user2Id AND cr.user2Id = :user1Id))")
    Optional<ChatRoom> findDirectChatRoom(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id,
            @Param("organizationId") Long organizationId
    );
    
    /**
     * Find all chat rooms for a user (direct chats where user is participant)
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.organizationId = :organizationId " +
           "AND (cr.user1Id = :userId OR cr.user2Id = :userId)")
    java.util.List<ChatRoom> findDirectChatRoomsByUser(
            @Param("userId") Long userId,
            @Param("organizationId") Long organizationId
    );
}

