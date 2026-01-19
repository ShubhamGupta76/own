/**
 * Chat Page Component
 * Real-time chat for channels and direct messages
 */

import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { chatApi, userApi } from '../api';
import { wsManager } from '../services/websocket';
import { useAuth } from '../contexts/AuthContext';
import type { Message, ChatEvent, SendMessageRequest, User } from '../types/api';

interface ChatPageProps {
  channelId?: number;
  userId?: number;
  roomType?: 'channel' | 'direct';
}

export const ChatPage: React.FC<ChatPageProps> = ({ channelId, userId, roomType = 'channel' }) => {
  const { userId: routeUserId } = useParams<{ userId?: string }>();
  const { user: currentUser } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [userMap, setUserMap] = useState<Map<number, User>>(new Map());
  const [currentChatRoomId, setCurrentChatRoomId] = useState<number | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const actualUserId = userId || (routeUserId ? Number(routeUserId) : undefined);
  const actualRoomType = roomType || (channelId ? 'channel' : 'direct');
  const roomId = channelId || actualUserId || 0;

  // Fetch user details for message senders
  useEffect(() => {
    const fetchUserDetails = async () => {
      if (messages.length === 0) return;
      
      const senderIds = new Set(messages.map(m => m.senderId));
      const missingIds = Array.from(senderIds).filter(id => !userMap.has(id));
      
      if (missingIds.length === 0) return;
      
      try {
        // Fetch organization members to get user details
        const members = await userApi.getOrganizationMembers();
        const newUserMap = new Map(userMap);
        members.forEach(user => {
          if (senderIds.has(user.id)) {
            newUserMap.set(user.id, user);
          }
        });
        setUserMap(newUserMap);
      } catch (err) {
        console.error('Error fetching user details:', err);
      }
    };
    
    fetchUserDetails();
  }, [messages, userMap]);

  // Scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Fetch initial messages
  useEffect(() => {
    const fetchMessages = async () => {
      try {
        setIsLoading(true);
        setError('');
        let response;
        if (actualRoomType === 'channel' && channelId) {
          response = await chatApi.getChannelMessages(channelId, 0, 50);
        } else if (actualRoomType === 'direct' && actualUserId) {
          response = await chatApi.getDirectMessages(actualUserId, 0, 50);
        } else {
          return;
        }
        // Backend returns List<Message> directly, not PaginatedResponse
        // Handle both formats: array directly or PaginatedResponse with content
        const messagesArray = Array.isArray(response) ? response : (response.content || []);
        setMessages(messagesArray);
        
        // Store the chatRoomId from the first message (all messages in a chat have the same chatRoomId)
        if (messagesArray.length > 0 && messagesArray[0].chatRoomId) {
          setCurrentChatRoomId(messagesArray[0].chatRoomId);
        }
      } catch (err: any) {
        const status = err.response?.status;
        const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message || 'Failed to load messages';
        
        // Handle 404 specifically
        if (status === 404) {
          setError('Channel or messages not found. The channel may not exist or you may not have access.');
        } else {
          setError(errorMessage);
        }
        console.error('Error fetching messages:', err);
      } finally {
        setIsLoading(false);
      }
    };

    if (roomId > 0) {
      fetchMessages();
    }
  }, [roomId, actualRoomType, channelId, actualUserId]);

  // Connect to WebSocket
  useEffect(() => {
    if (roomId > 0) {
      wsManager.connectToChat(roomId, actualRoomType, (event: ChatEvent) => {
        if (event.type === 'MESSAGE_RECEIVED' && event.message) {
          const message = event.message;
          
          // For direct chats, accept messages from either participant
          // The backend broadcasts to both users' topics, so we receive messages for all our direct chats
          // We filter to only show messages from the chat we're currently viewing
          if (actualRoomType === 'direct' && actualUserId) {
            // Accept message if:
            // 1. It belongs to the current chat room (if we know it), OR
            // 2. The sender is the other user we're chatting with, OR
            // 3. The sender is the current user (we sent it)
            const isFromOtherUser = message.senderId === actualUserId;
            const isFromCurrentUser = message.senderId === currentUser?.id;
            const isFromCurrentChat = currentChatRoomId ? message.chatRoomId === currentChatRoomId : true;
            
            if ((isFromOtherUser || isFromCurrentUser) && isFromCurrentChat) {
              setMessages((prev) => {
                // Avoid duplicates
                if (prev.some(m => m.id === message.id)) {
                  return prev;
                }
                // Update chatRoomId if we don't have it yet
                if (!currentChatRoomId && message.chatRoomId) {
                  setCurrentChatRoomId(message.chatRoomId);
                }
                console.log('Adding message to chat:', {
                  messageId: message.id,
                  senderId: message.senderId,
                  content: message.content.substring(0, 50),
                  chatRoomId: message.chatRoomId
                });
                return [...prev, message];
              });
            } else {
              console.log('Filtered out message (not from current chat):', {
                messageId: message.id,
                senderId: message.senderId,
                chatRoomId: message.chatRoomId,
                currentChatRoomId,
                actualUserId,
                currentUserId: currentUser?.id,
                isFromOtherUser,
                isFromCurrentUser,
                isFromCurrentChat
              });
            }
          } else {
            // For channels, add all messages (they're already filtered by channel)
            setMessages((prev) => {
              // Avoid duplicates
              if (prev.some(m => m.id === message.id)) {
                return prev;
              }
              return [...prev, message];
            });
          }
        }
      });
    }

    return () => {
      if (roomId > 0) {
        wsManager.disconnectFromChat(roomId, actualRoomType);
      }
    };
  }, [roomId, actualRoomType, actualUserId, currentUser?.id]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !roomId) return;

    try {
      // Use roomId as chatRoomId (backend will handle the mapping)
      // For direct chats, roomId is the other user's ID
      const chatRoomId = roomId;

      const request: SendMessageRequest = {
        chatRoomId,
        content: newMessage.trim(),
        messageType: 'TEXT',
      };

      console.log('Sending message:', {
        chatRoomId,
        roomId,
        actualRoomType,
        actualUserId,
        content: newMessage.trim().substring(0, 50)
      });

      const message: Message = await chatApi.sendMessage(request);
      
      console.log('Message sent successfully:', {
        messageId: message.id,
        chatRoomId: message.chatRoomId,
        senderId: message.senderId
      });
      
      // Update chatRoomId if we don't have it yet
      if (!currentChatRoomId && message.chatRoomId) {
        setCurrentChatRoomId(message.chatRoomId);
      }
      
      setMessages((prev) => {
        // Avoid duplicates
        if (prev.some(m => m.id === message.id)) {
          return prev;
        }
        return [...prev, message];
      });
      setNewMessage('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to send message');
      console.error('Error sending message:', err);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-white">
      {/* Chat Header */}
      <div className="border-b border-gray-200 px-6 py-4">
        <h2 className="text-lg font-semibold text-gray-900">
          {actualRoomType === 'channel' ? `# Channel ${channelId}` : `Direct Message ${actualUserId}`}
        </h2>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {messages.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-4xl mb-4">💬</div>
            <p className="text-gray-500">No messages yet. Start the conversation!</p>
          </div>
        ) : (
          messages.map((message) => {
            const sender = userMap.get(message.senderId);
            const senderName = sender 
              ? (sender.displayName || `${sender.firstName} ${sender.lastName}` || sender.email)
              : `User ${message.senderId}`;
            const senderInitial = sender
              ? (sender.firstName?.charAt(0) || sender.email.charAt(0).toUpperCase())
              : message.senderId.toString().charAt(0);
            const isCurrentUser = message.senderId === currentUser?.id;
            
            return (
              <div key={message.id} className={`flex items-start space-x-3 ${isCurrentUser ? 'flex-row-reverse space-x-reverse' : ''}`}>
                <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-medium flex-shrink-0">
                  {senderInitial}
                </div>
                <div className="flex-1 min-w-0">
                  <div className={`flex items-center space-x-2 mb-1 ${isCurrentUser ? 'justify-end' : ''}`}>
                    <span className="text-sm font-medium text-gray-900">
                      {isCurrentUser ? 'You' : senderName}
                    </span>
                    <span className="text-xs text-gray-500">
                      {new Date(message.createdAt).toLocaleTimeString()}
                    </span>
                  </div>
                  <div className={`text-sm text-gray-700 whitespace-pre-wrap break-words ${isCurrentUser ? 'text-right' : ''}`}>
                    {message.content}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Message Input */}
      <div className="border-t border-gray-200 px-6 py-4">
        <form onSubmit={handleSendMessage} className="flex items-end space-x-4">
          <div className="flex-1">
            <textarea
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage(e);
                }
              }}
              placeholder="Type a message..."
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
            />
          </div>
          <button
            type="submit"
            disabled={!newMessage.trim()}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
};

