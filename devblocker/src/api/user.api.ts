/**
 * User API Service
 * Handles user-related endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { User } from '../types/api';

export const userApi = {
  /**
   * Get current user profile
   * Note: This endpoint may not exist - use getUserById instead
   */
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get<User>(API_CONFIG.ENDPOINTS.USER.PROFILE);
    return response.data;
  },

  /**
   * Get current user (alias for getProfile)
   */
  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>(API_CONFIG.ENDPOINTS.USER.PROFILE);
    return response.data;
  },

  /**
   * Get user by ID (Admin only)
   */
  getUserById: async (userId: number): Promise<User> => {
    const response = await apiClient.get<User>(API_CONFIG.ENDPOINTS.USER.USER_BY_ID(userId));
    return response.data;
  },

  /**
   * Get all users (Admin only)
   */
  getUsers: async (): Promise<User[]> => {
    const response = await apiClient.get<User[]>(API_CONFIG.ENDPOINTS.USER.USERS);
    return response.data;
  },
};

