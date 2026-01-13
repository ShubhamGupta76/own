/**
 * Auth API Service
 * Handles authentication endpoints
 */

import { apiClient } from './index';
import { API_CONFIG, setToken, removeToken } from '../config/api';
import type { RegisterRequest, LoginRequest, LoginResponse } from '../types/api';

export const authApi = {
  /**
   * Admin Registration
   */
  register: async (data: RegisterRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER,
      data
    );
    if (response.data.token) {
      setToken(response.data.token);
      if (response.data.userId) {
        localStorage.setItem('user_id', response.data.userId.toString());
      }
      if (response.data.organizationId) {
        localStorage.setItem('organization_id', response.data.organizationId.toString());
      }
      if (response.data.email) {
        localStorage.setItem('user_email', response.data.email);
      }
      if (response.data.role) {
        localStorage.setItem('user_role', response.data.role);
      }
    }
    return response.data;
  },

  /**
   * Admin/Manager Login
   */
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.LOGIN,
      credentials
    );
    if (response.data.token) {
      setToken(response.data.token);
      if (response.data.userId) {
        localStorage.setItem('user_id', response.data.userId.toString());
      }
      if (response.data.organizationId) {
        localStorage.setItem('organization_id', response.data.organizationId.toString());
      }
      if (response.data.email) {
        localStorage.setItem('user_email', response.data.email);
      }
      if (response.data.role) {
        localStorage.setItem('user_role', response.data.role);
      }
    }
    return response.data;
  },

  /**
   * Employee Login
   */
  employeeLogin: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.EMPLOYEE_LOGIN,
      credentials
    );
    if (response.data.token) {
      setToken(response.data.token);
      if (response.data.userId) {
        localStorage.setItem('user_id', response.data.userId.toString());
      }
      if (response.data.organizationId) {
        localStorage.setItem('organization_id', response.data.organizationId.toString());
      }
      if (response.data.email) {
        localStorage.setItem('user_email', response.data.email);
      }
      if (response.data.role) {
        localStorage.setItem('user_role', response.data.role);
      }
    }
    return response.data;
  },

  /**
   * Organization Login
   */
  organizationLogin: async (credentials: LoginRequest): Promise<LoginResponse> => {
    console.log('[AUTH] Starting organization login for:', credentials.email);
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.ORGANIZATION_LOGIN,
      credentials
    );
    console.log('[AUTH] Organization login response:', JSON.stringify(response.data, null, 2));
    console.log('[AUTH] Response status:', response.status);
    console.log('[AUTH] Response headers:', response.headers);
    
    if (response.data.token) {
      console.log('[AUTH] Token received, length:', response.data.token.length);
      setToken(response.data.token);
      console.log('[AUTH] Token stored in localStorage');
      
      // Verify token was stored
      const storedToken = localStorage.getItem('jwt_token');
      console.log('[AUTH] Verification - token in localStorage:', storedToken ? `Yes (${storedToken.length} chars)` : 'NO!');
      
      if (response.data.userId) {
        localStorage.setItem('user_id', response.data.userId.toString());
      }
      if (response.data.organizationId) {
        localStorage.setItem('organization_id', response.data.organizationId.toString());
      }
      if (response.data.email) {
        localStorage.setItem('user_email', response.data.email);
      }
      if (response.data.role) {
        localStorage.setItem('user_role', response.data.role);
      }
    } else {
      console.error('[AUTH] No token in login response!', response.data);
      console.error('[AUTH] Response keys:', Object.keys(response.data));
    }
    return response.data;
  },

  /**
   * Logout
   */
  logout: (): void => {
    removeToken();
  },
};

