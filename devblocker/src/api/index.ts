/**
 * API Client Configuration and Exports
 * Centralized axios instance with interceptors
 */

import axios from 'axios';
import type { AxiosInstance, AxiosError } from 'axios';
import { API_CONFIG, getToken, removeToken, decodeJWT } from '../config/api';
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
  (error: AxiosError<ApiError | { message?: string; errorCode?: string }>) => {
    const url = error.config?.url || '';
    const isAuthEndpoint = url.includes('/auth/login') || 
                          url.includes('/auth/register') || 
                          url.includes('/auth/employee/login');
    const currentPath = window.location.pathname;
    
    // Handle 401 Unauthorized
    if (error.response?.status === 401 && !isAuthEndpoint) {
      removeToken();
      if (currentPath !== '/login' && currentPath !== '/register') {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
    
    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      // Don't redirect, let the component handle it
      console.warn('Access denied:', error.response.data);
      return Promise.reject(error);
    }
    
    // Handle 404 with ORGANIZATION_NOT_CREATED error code
    if (error.response?.status === 404) {
      const errorData = error.response.data as ApiError;
      const url = error.config?.url || '';
      const isOrganizationEndpoint = url.includes('/organizations/my-organization');
      
      if (isOrganizationEndpoint && 
          (errorData?.errorCode === 'ORGANIZATION_NOT_CREATED' || 
           (errorData?.message && errorData.message.toLowerCase().includes('organization not found') && 
            errorData.message.toLowerCase().includes('create')))) {
        // This is an expected 404 - organization doesn't exist yet
        // Suppress console error for expected business logic
        // Redirect to onboarding/create organization page
        if (currentPath !== '/admin/onboarding' && currentPath !== '/register-org') {
          // Only redirect if user is ADMIN (check from token or context)
          const token = getToken();
          if (token) {
            try {
              const decoded = decodeJWT(token);
              if (decoded?.role === 'ADMIN') {
                window.location.href = '/admin/onboarding';
                return Promise.reject(error);
              }
            } catch (e) {
              // If we can't decode token, continue with normal error handling
            }
          }
        }
        // Return rejected promise but don't log to console (expected error)
        return Promise.reject(error);
      }
    }
    
    return Promise.reject(error);
  }
);

// Export all API services
export { authApi } from './auth.api';
export { userApi } from './user.api';
export { organizationApi } from './organization.api';
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

