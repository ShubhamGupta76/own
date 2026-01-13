/**
 * Notification API Service
 * Handles notification endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { Notification } from '../types/api';

export const notificationsApi = {
  /**
   * Get all notifications
   */
  getNotifications: async (): Promise<Notification[]> => {
    const response = await apiClient.get<Notification[]>(
      API_CONFIG.ENDPOINTS.NOTIFICATIONS.LIST
    );
    return response.data;
  },

  /**
   * Get unread notifications
   */
  getUnreadNotifications: async (): Promise<Notification[]> => {
    const response = await apiClient.get<Notification[]>(
      API_CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD
    );
    return response.data;
  },

  /**
   * Get unread count
   */
  getUnreadCount: async (): Promise<number> => {
    const response = await apiClient.get<{ count: number }>(
      API_CONFIG.ENDPOINTS.NOTIFICATIONS.UNREAD_COUNT
    );
    return response.data.count;
  },

  /**
   * Mark notification as read
   */
  markAsRead: async (notificationId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_READ(notificationId));
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async (): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.NOTIFICATIONS.MARK_ALL_READ);
  },

  /**
   * Get activity feed
   */
  getActivityFeed: async (limit: number = 50): Promise<Notification[]> => {
    const response = await apiClient.get<Notification[]>(
      `${API_CONFIG.ENDPOINTS.NOTIFICATIONS.ACTIVITY}?limit=${limit}`
    );
    return response.data;
  },
};

