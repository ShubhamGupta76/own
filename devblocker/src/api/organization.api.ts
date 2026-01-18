/**
 * Organization API Service
 * Handles organization-related endpoints
 */

import { apiClient } from './index';
import { API_CONFIG, setToken } from '../config/api';
import type { Organization, ApiError } from '../types/api';

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
   * Returns new JWT token with organizationId
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
   * Throws error with errorCode: ORGANIZATION_NOT_CREATED if organization doesn't exist
   */
  getMyOrganization: async (): Promise<Organization> => {
    try {
      const response = await apiClient.get<Organization>(
        API_CONFIG.ENDPOINTS.ORGANIZATION.MY_ORGANIZATION
      );
      return response.data;
    } catch (error: any) {
      // Re-throw with proper error structure
      if (error.response?.status === 404) {
        const errorData = error.response.data as ApiError;
        if (errorData?.errorCode === 'ORGANIZATION_NOT_CREATED' || 
            (errorData?.message && errorData.message.toLowerCase().includes('organization not found'))) {
          throw {
            ...error,
            response: {
              ...error.response,
              data: {
                ...errorData,
                errorCode: 'ORGANIZATION_NOT_CREATED',
                message: errorData.message || 'Organization not found. Please create an organization first.',
              },
            },
          };
        }
      }
      throw error;
    }
  },
};

