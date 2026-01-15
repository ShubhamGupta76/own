/**
 * Organization API Service
 * Handles organization-related endpoints
 */

import { apiClient } from './index';
import { API_CONFIG, setToken } from '../config/api';
import type { Organization } from '../types/api';

export interface OrganizationRequest {
  name: string;
  domain?: string;
}

export interface OrganizationResponse {
  organization: Organization;
  token: string;
  userId: number;
  email: string;
  role: string;
  organizationId: number;
  message?: string;
}

export const organizationApi = {
  /**
   * Create a new organization
   */
  createOrganization: async (request: OrganizationRequest): Promise<OrganizationResponse> => {
    const response = await apiClient.post<OrganizationResponse>(
      API_CONFIG.ENDPOINTS.ORGANIZATION.CREATE,
      request
    );
    
    // Update token if provided - this updates localStorage
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
   * Get my organization
   */
  getMyOrganization: async (): Promise<Organization> => {
    const response = await apiClient.get<Organization>(
      API_CONFIG.ENDPOINTS.ORGANIZATION.MY_ORGANIZATION
    );
    return response.data;
  },
};

