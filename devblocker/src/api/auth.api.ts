/**
 * Auth API Service
 * Handles authentication endpoints
 */

import { apiClient } from './index';
import { API_CONFIG, setToken, removeToken } from '../config/api';
import type { RegisterRequest, LoginRequest, LoginResponse, OrganizationRegistrationRequest } from '../types/api';

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
   * Organization Registration (creates admin + organization)
   */
  registerOrganization: async (data: OrganizationRegistrationRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER_ORGANIZATION,
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
    const response = await apiClient.post<LoginResponse>(
      API_CONFIG.ENDPOINTS.AUTH.ORGANIZATION_LOGIN,
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
   * Logout
   */
  logout: (): void => {
    removeToken();
  },
};

