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

  /**
   * Create a new user (Admin only)
   */
  createUser: async (userData: {
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    password?: string;
  }): Promise<User> => {
    const response = await apiClient.post<User>(API_CONFIG.ENDPOINTS.USER.USERS, userData);
    return response.data;
  },

  /**
   * Reset user password (Admin only)
   */
  resetPassword: async (userId: number, newPassword: string): Promise<User> => {
    const response = await apiClient.put<User>(
      `${API_CONFIG.ENDPOINTS.USER.USERS}/${userId}/password`,
      { password: newPassword }
    );
    return response.data;
  },

  /**
   * Get organization members (for adding to teams/channels)
   * Accessible by ADMIN, MANAGER, and EMPLOYEE
   */
  getOrganizationMembers: async (): Promise<User[]> => {
    const response = await apiClient.get<User[]>(`${API_CONFIG.ENDPOINTS.USER.USERS}/organization/members`);
    return response.data;
  },
};

