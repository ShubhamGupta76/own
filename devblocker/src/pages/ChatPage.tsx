/**
 * Chat Page Component
 * Real-time chat for channels and direct messages
 */

import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { chatApi } from '../api';
import { wsManager } from '../services/websocket';
import type { Message, ChatEvent, SendMessageRequest } from '../types/api';

interface ChatPageProps {
  channelId?: number;
  userId?: number;
  roomType?: 'channel' | 'direct';
}

export const ChatPage: React.FC<ChatPageProps> = ({ channelId, userId, roomType = 'channel' }) => {
  const { userId: routeUserId } = useParams<{ userId?: string }>();
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const actualUserId = userId || (routeUserId ? Number(routeUserId) : undefined);
  const actualRoomType = roomType || (channelId ? 'channel' : 'direct');
  const roomId = channelId || actualUserId || 0;

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
          setMessages((prev) => [...prev, event.message!]);
        }
      });
    }

    return () => {
      if (roomId > 0) {
        wsManager.disconnectFromChat(roomId, actualRoomType);
      }
    };
  }, [roomId, actualRoomType]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !roomId) return;

    try {
      // Use roomId as chatRoomId (backend will handle the mapping)
      const chatRoomId = roomId;

      const request: SendMessageRequest = {
        chatRoomId,
        content: newMessage.trim(),
        messageType: 'TEXT',
      };

      const message: Message = await chatApi.sendMessage(request);
      setMessages((prev) => [...prev, message]);
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
          messages.map((message) => (
            <div key={message.id} className="flex items-start space-x-3">
              <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-medium flex-shrink-0">
                {message.senderId.toString().charAt(0)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center space-x-2 mb-1">
                  <span className="text-sm font-medium text-gray-900">
                    User {message.senderId}
                  </span>
                  <span className="text-xs text-gray-500">
                    {new Date(message.createdAt).toLocaleTimeString()}
                  </span>
                </div>
                <div className="text-sm text-gray-700 whitespace-pre-wrap break-words">
                  {message.content}
                </div>
              </div>
            </div>
          ))
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

