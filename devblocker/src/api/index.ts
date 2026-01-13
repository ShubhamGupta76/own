/**
 * API Client Configuration and Exports
 * Centralized axios instance with interceptors
 */

import axios from 'axios';
import type { AxiosInstance, AxiosError } from 'axios';
import { API_CONFIG, getToken, removeToken } from '../config/api';
import type { ApiError } from '../types/api';

/**
 * Create axios instance with default config
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor: Add JWT token to requests
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor: Handle errors and token expiration
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError | { message?: string }>) => {
    // Don't redirect on 401 for auth endpoints (login/register)
    const url = error.config?.url || '';
    const isAuthEndpoint = url.includes('/auth/login') || 
                          url.includes('/auth/register') || 
                          url.includes('/auth/employee/login');
    
    if (error.response?.status === 401 && !isAuthEndpoint) {
      // Unauthorized - token expired or invalid (but not for auth endpoints)
      removeToken();
      // Only redirect if we're not already on the login page
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Export all API services
export { authApi } from './auth.api';
export { userApi } from './user.api';
export { teamsApi } from './team.api';
export { channelsApi } from './channel.api';
export { chatApi } from './chat.api';
export { meetingsApi } from './meeting.api';
export { filesApi } from './file.api';
export { tasksApi } from './task.api';
export { notificationsApi } from './notification.api';

// Export API config for reference
export { API_CONFIG } from '../config/api';

// Export default client
export default apiClient;

