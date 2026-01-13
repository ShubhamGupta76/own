package com.connect.Chat.service;

import com.connect.Chat.dto.MessageResponse;
import com.connect.Chat.dto.SendMessageRequest;
import com.connect.Chat.entity.ChatPolicy;
import com.connect.Chat.entity.ChatRoom;
import com.connect.Chat.entity.Message;
import com.connect.Chat.repository.ChatPolicyRepository;
import com.connect.Chat.repository.ChatRoomRepository;
import com.connect.Chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for chat operations
 * Handles sending/receiving messages and chat room management
 */
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatPolicyRepository chatPolicyRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatEventProducer eventProducer;
    
    /**
     * Send a message
     * Validates chat policy, saves message, and broadcasts via WebSocket
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId, String senderRole, Long organizationId) {
        // Check if chat is enabled for the organization
        ChatPolicy policy = chatPolicyRepository.findByOrganizationId(organizationId)
                .orElse(ChatPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .build());
        
        if (!policy.getEnabled()) {
            throw new RuntimeException("Chat is disabled for your organization");
        }
        
        // Verify chat room exists and belongs to organization
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        if (!chatRoom.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Chat room does not belong to your organization");
        }
        
        // Convert sender role string to enum
        Message.SenderRole roleEnum;
        try {
            roleEnum = Message.SenderRole.valueOf(senderRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid sender role: " + senderRole);
        }
        
        // Validate message content based on type
        validateMessageContent(request);
        
        // Create and save message
        Message message = Message.builder()
                .chatRoomId(request.getChatRoomId())
                .senderId(senderId)
                .senderRole(roleEnum)
                .organizationId(organizationId)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .fileUrl(request.getFileUrl())
                .fileId(request.getFileId())
                .linkUrl(request.getLinkUrl())
                .emojiCode(request.getEmojiCode())
                .gifUrl(request.getGifUrl())
                .metadata(request.getMetadata())
                .status(Message.MessageStatus.SENT)
                .build();
        
        message = messageRepository.save(message);
        
        // Extract mentioned user IDs from message content
        List<Long> mentionedUserIds = extractMentions(request.getContent());
        
        // Publish Kafka event for async notification processing
        eventProducer.publishMessageSentEvent(
                message.getId(),
                chatRoom.getRoomId(), // channelId if channel type
                request.getChatRoomId(),
                senderId,
                senderRole,
                mentionedUserIds,
                organizationId,
                request.getContent()
        );
        
        // Broadcast message via WebSocket
        MessageResponse messageResponse = mapToResponse(message);
        
        // Determine WebSocket topic based on chat room type
        if (chatRoom.getRoomType() == ChatRoom.RoomType.DIRECT) {
            // For direct chat, send to both users
            broadcastDirectMessage(messageResponse, chatRoom);
        } else {
            // For channel/team, send to room topic
            String topic = getWebSocketTopic(chatRoom);
            messagingTemplate.convertAndSend(topic, messageResponse);
        }
        
        return messageResponse;
    }
    
    /**
     * Get messages for a channel
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getChannelMessages(Long channelId, Long organizationId, int page, int size) {
        // Find or create chat room for channel
        ChatRoom chatRoom = chatRoomRepository
                .findByRoomTypeAndRoomIdAndOrganizationId(ChatRoom.RoomType.CHANNEL, channelId, organizationId)
                .orElseThrow(() -> new RuntimeException("Channel chat room not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), pageable);
        
        return messages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get messages for direct chat with a user
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getUserMessages(Long userId, Long currentUserId, Long organizationId, int page, int size) {
        // Find or create direct chat room
        ChatRoom chatRoom = chatRoomRepository.findDirectChatRoom(currentUserId, userId, organizationId)
                .orElseGet(() -> {
                    // Create new direct chat room if it doesn't exist
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomType(ChatRoom.RoomType.DIRECT)
                            .roomId(null)
                            .user1Id(currentUserId)
                            .user2Id(userId)
                            .organizationId(organizationId)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), pageable);
        
        return messages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get or create chat room for a channel
     */
    @Transactional
    public ChatRoom getOrCreateChannelRoom(Long channelId, Long organizationId) {
        return chatRoomRepository
                .findByRoomTypeAndRoomIdAndOrganizationId(ChatRoom.RoomType.CHANNEL, channelId, organizationId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomType(ChatRoom.RoomType.CHANNEL)
                            .roomId(channelId)
                            .organizationId(organizationId)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }
    
    /**
     * Get or create chat room for a team
     */
    @Transactional
    public ChatRoom getOrCreateTeamRoom(Long teamId, Long organizationId) {
        return chatRoomRepository
                .findByRoomTypeAndRoomIdAndOrganizationId(ChatRoom.RoomType.TEAM, teamId, organizationId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomType(ChatRoom.RoomType.TEAM)
                            .roomId(teamId)
                            .organizationId(organizationId)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }
    
    /**
     * Validate message content based on message type
     */
    private void validateMessageContent(SendMessageRequest request) {
        switch (request.getMessageType()) {
            case TEXT:
                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                    throw new RuntimeException("Content is required for TEXT messages");
                }
                break;
            case FILE:
                if (request.getFileUrl() == null && request.getFileId() == null) {
                    throw new RuntimeException("File URL or File ID is required for FILE messages");
                }
                break;
            case LINK:
                if (request.getLinkUrl() == null || request.getLinkUrl().trim().isEmpty()) {
                    throw new RuntimeException("Link URL is required for LINK messages");
                }
                break;
            case EMOJI:
                if (request.getEmojiCode() == null || request.getEmojiCode().trim().isEmpty()) {
                    throw new RuntimeException("Emoji code is required for EMOJI messages");
                }
                break;
            case GIF:
                if (request.getGifUrl() == null || request.getGifUrl().trim().isEmpty()) {
                    throw new RuntimeException("GIF URL is required for GIF messages");
                }
                break;
            default:
                // SYSTEM messages don't need validation
                break;
        }
    }
    
    /**
     * Map Message entity to MessageResponse DTO
     */
    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole().name())
                .organizationId(message.getOrganizationId())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .fileUrl(message.getFileUrl())
                .fileId(message.getFileId())
                .linkUrl(message.getLinkUrl())
                .emojiCode(message.getEmojiCode())
                .gifUrl(message.getGifUrl())
                .metadata(message.getMetadata())
                .status(message.getStatus().name())
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    /**
     * Extract mentioned user IDs from message content
     * Looks for @userId patterns in the message
     * Format: @123, @456, etc.
     */
    private List<Long> extractMentions(String content) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }
        
        // Simple regex to find @userId patterns
        // In production, this should be more sophisticated and validate user IDs
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        List<Long> mentionedIds = new java.util.ArrayList<>();
        while (matcher.find()) {
            try {
                Long userId = Long.parseLong(matcher.group(1));
                if (!mentionedIds.contains(userId)) {
                    mentionedIds.add(userId);
                }
            } catch (NumberFormatException e) {
                // Skip invalid user IDs
            }
        }
        
        return mentionedIds;
    }
    
    /**
     * Get WebSocket topic for a chat room
     */
    private String getWebSocketTopic(ChatRoom chatRoom) {
        switch (chatRoom.getRoomType()) {
            case CHANNEL:
                return "/topic/channel/" + chatRoom.getRoomId();
            case TEAM:
                return "/topic/team/" + chatRoom.getRoomId();
            case DIRECT:
                // For direct chat, send to both users' topics
                // Note: Frontend should subscribe to /topic/user/{userId} for direct chats
                return "/topic/user/" + chatRoom.getUser1Id();
            default:
                return "/topic/chat/" + chatRoom.getId();
        }
    }
    
    /**
     * Broadcast message to direct chat (both users)
     */
    private void broadcastDirectMessage(MessageResponse messageResponse, ChatRoom chatRoom) {
        // Send to both users in direct chat
        messagingTemplate.convertAndSend("/topic/user/" + chatRoom.getUser1Id(), messageResponse);
        messagingTemplate.convertAndSend("/topic/user/" + chatRoom.getUser2Id(), messageResponse);
    }
}

