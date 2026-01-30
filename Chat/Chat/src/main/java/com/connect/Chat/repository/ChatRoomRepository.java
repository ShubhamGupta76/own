package com.connect.Chat.repository;

import com.connect.Chat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatRoom entity
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    
    /**
     * Find chat room by type and room ID
     */
    Optional<ChatRoom> findByRoomTypeAndRoomIdAndOrganizationId(
            ChatRoom.RoomType roomType, 
            String roomId, 
            String organizationId
    );
    
    /**
     * Find direct chat room between two users
     */
    @Query("{ 'roomType': 'DIRECT', 'organizationId': ?2, $or: [ { 'user1Id': ?0, 'user2Id': ?1 }, { 'user1Id': ?1, 'user2Id': ?0 } ] }")
    Optional<ChatRoom> findDirectChatRoom(
            String user1Id,
            String user2Id,
            String organizationId
    );
    
    /**
     * Find all chat rooms for a user (direct chats where user is participant)
     */
    @Query("{ 'organizationId': ?1, $or: [ { 'user1Id': ?0 }, { 'user2Id': ?0 } ] }")
    List<ChatRoom> findDirectChatRoomsByUser(
            String userId,
            String organizationId
    );
}

