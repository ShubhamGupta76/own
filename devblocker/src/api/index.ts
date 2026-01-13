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
    // Always log to help debug
    const token = getToken();
    const allKeys = Object.keys(localStorage);
    const directToken = localStorage.getItem('jwt_token');
    
    console.log('========================================');
    console.log('[API INTERCEPTOR] Request to:', config.url);
    console.log('[API INTERCEPTOR] Method:', config.method?.toUpperCase());
    console.log('[API INTERCEPTOR] Token from getToken():', token ? `Yes (${token.length} chars)` : 'NO');
    console.log('[API INTERCEPTOR] localStorage keys:', allKeys);
    console.log('[API INTERCEPTOR] jwt_token directly:', directToken ? `EXISTS (${directToken.length} chars)` : 'MISSING');
    console.log('[API INTERCEPTOR] Token preview:', token ? token.substring(0, 30) + '...' : 'null');
    
    if (token && config.headers) {
      const authValue = `Bearer ${token}`;
      config.headers.Authorization = authValue;
      console.log('[API INTERCEPTOR] ✓ Authorization header added');
      console.log('[API INTERCEPTOR] Header value preview:', authValue.substring(0, 40) + '...');
      console.log('[API INTERCEPTOR] Full headers:', JSON.stringify(config.headers, null, 2));
    } else {
      console.error('[API INTERCEPTOR] ✗ CRITICAL: No token available!');
      console.error('[API INTERCEPTOR] getToken() returned:', token);
      console.error('[API INTERCEPTOR] localStorage.getItem("jwt_token"):', directToken);
    }
    console.log('========================================');
    return config;
  },
  (error) => {
    console.error('[API INTERCEPTOR] Request error:', error);
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
      console.error('[API] 401 Unauthorized for:', url);
      console.error('[API] Error response:', error.response?.data);
      console.error('[API] Current token in localStorage:', localStorage.getItem('jwt_token') ? 'exists' : 'missing');
      
      // Don't remove token immediately - might be a temporary issue
      // Only remove if we're sure it's invalid
      // removeToken();
      
      // Only redirect if we're not already on the login page
      // if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
      //   window.location.href = '/login';
      // }
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

