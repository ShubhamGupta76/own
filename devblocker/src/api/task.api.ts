/**
 * Task API Service
 * Handles task management endpoints (Jira-like functionality)
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { Task, CreateTaskRequest, TaskComment } from '../types/api';

export const tasksApi = {
  /**
   * Create task
   */
  createTask: async (task: CreateTaskRequest): Promise<Task> => {
    const response = await apiClient.post<Task>(API_CONFIG.ENDPOINTS.TASKS.CREATE, task);
    return response.data;
  },

  /**
   * Assign task
   */
  assignTask: async (taskId: string, userId: string): Promise<void> => {
    await apiClient.put(API_CONFIG.ENDPOINTS.TASKS.ASSIGN(taskId), { userId });
  },

  /**
   * Update task status
   */
  updateTaskStatus: async (
    taskId: string,
    status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'DONE'
  ): Promise<void> => {
    await apiClient.put(API_CONFIG.ENDPOINTS.TASKS.UPDATE_STATUS(taskId), { status });
  },

  /**
   * Add task comment
   */
  addComment: async (taskId: string, content: string): Promise<TaskComment> => {
    const response = await apiClient.post<TaskComment>(
      API_CONFIG.ENDPOINTS.TASKS.COMMENT(taskId),
      { content }
    );
    return response.data;
  },

  /**
   * Get tasks by channel
   */
  getTasksByChannel: async (channelId: string): Promise<Task[]> => {
    const response = await apiClient.get<Task[]>(
      API_CONFIG.ENDPOINTS.TASKS.BY_CHANNEL(channelId)
    );
    return response.data;
  },

  /**
   * Get task by ID
   */
  getTask: async (taskId: string): Promise<Task> => {
    const response = await apiClient.get<Task>(API_CONFIG.ENDPOINTS.TASKS.BY_ID(taskId));
    return response.data;
  },
};

