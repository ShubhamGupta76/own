/**
 * Team API Service
 * Handles team-related endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { Team } from '../types/api';

export const teamsApi = {
  /**
   * Get all teams in organization
   */
  getTeams: async (): Promise<Team[]> => {
    const response = await apiClient.get<Team[]>(API_CONFIG.ENDPOINTS.TEAMS.LIST);
    return response.data;
  },

  /**
   * Get teams for current user
   */
  getMyTeams: async (): Promise<Team[]> => {
    const response = await apiClient.get<Team[]>(API_CONFIG.ENDPOINTS.TEAMS.MY_TEAMS);
    return response.data;
  },

  /**
   * Create new team
   */
  createTeam: async (team: { name: string; description?: string }): Promise<Team> => {
    const response = await apiClient.post<Team>(API_CONFIG.ENDPOINTS.TEAMS.CREATE, team);
    return response.data;
  },

  /**
   * Add member to team
   */
  addMember: async (teamId: number, userId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.TEAMS.ADD_MEMBER(teamId), { userId });
  },

  /**
   * Remove member from team
   */
  removeMember: async (teamId: number, userId: number): Promise<void> => {
    await apiClient.delete(API_CONFIG.ENDPOINTS.TEAMS.REMOVE_MEMBER(teamId, userId));
  },
};

