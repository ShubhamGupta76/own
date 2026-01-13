/**
 * Channel API Service
 * Handles channel-related endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { Channel } from '../types/api';

export const channelsApi = {
  /**
   * Get channels for a team
   */
  getChannelsByTeam: async (teamId: number): Promise<Channel[]> => {
    const response = await apiClient.get<Channel[]>(
      API_CONFIG.ENDPOINTS.CHANNELS.BY_TEAM(teamId)
    );
    return response.data;
  },

  /**
   * Create channel in team
   */
  createChannel: async (
    teamId: number,
    channel: { name: string; description?: string }
  ): Promise<Channel> => {
    const response = await apiClient.post<Channel>(
      API_CONFIG.ENDPOINTS.CHANNELS.CREATE(teamId),
      channel
    );
    return response.data;
  },

  /**
   * Add member to channel
   */
  addMember: async (channelId: number, userId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.CHANNELS.ADD_MEMBER(channelId), { userId });
  },

  /**
   * Remove member from channel
   */
  removeMember: async (channelId: number, userId: number): Promise<void> => {
    await apiClient.delete(API_CONFIG.ENDPOINTS.CHANNELS.REMOVE_MEMBER(channelId, userId));
  },
};

