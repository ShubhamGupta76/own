/**
 * Chat API Service
 * Handles chat and messaging endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { Message, SendMessageRequest, PaginatedResponse, Chat } from '../types/api';

export const chatApi = {
  /**
   * Send message
   */
  sendMessage: async (message: SendMessageRequest): Promise<Message> => {
    const response = await apiClient.post<Message>(
      API_CONFIG.ENDPOINTS.CHAT.SEND,
      message
    );
    return response.data;
  },

  /**
   * Get channel messages
   * Backend returns List<MessageResponse> directly, not PaginatedResponse
   */
  getChannelMessages: async (
    channelId: number,
    page: number = 0,
    size: number = 20
  ): Promise<Message[] | PaginatedResponse<Message>> => {
    const response = await apiClient.get<Message[] | PaginatedResponse<Message>>(
      `${API_CONFIG.ENDPOINTS.CHAT.CHANNEL_MESSAGES(channelId)}?page=${page}&size=${size}`
    );
    return response.data;
  },

  /**
   * Get direct messages
   * Backend returns List<MessageResponse> directly, not PaginatedResponse
   */
  getDirectMessages: async (
    userId: number,
    page: number = 0,
    size: number = 20
  ): Promise<Message[] | PaginatedResponse<Message>> => {
    const response = await apiClient.get<Message[] | PaginatedResponse<Message>>(
      `${API_CONFIG.ENDPOINTS.CHAT.DIRECT_MESSAGES(userId)}?page=${page}&size=${size}`
    );
    return response.data;
  },

  /**
   * Get all chats
   */
  getChats: async (filter?: string): Promise<Chat[]> => {
    const response = await apiClient.get<Chat[]>(
      `${API_CONFIG.ENDPOINTS.CHAT.CHATS}${filter ? `?filter=${filter}` : ''}`
    );
    return response.data;
  },

  /**
   * Get favorite chats
   */
  getFavoriteChats: async (): Promise<Chat[]> => {
    const response = await apiClient.get<Chat[]>(API_CONFIG.ENDPOINTS.CHAT.FAVORITE_CHATS);
    return response.data;
  },
};

