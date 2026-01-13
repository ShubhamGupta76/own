/**
 * File API Service
 * Handles file upload, download, and management endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type { AxiosRequestConfig } from 'axios';
import type { FileMetadata, UploadFileRequest } from '../types/api';

export const filesApi = {
  /**
   * Upload file
   */
  uploadFile: async (request: UploadFileRequest): Promise<FileMetadata> => {
    const formData = new FormData();
    formData.append('file', request.file);
    if (request.channelId) {
      formData.append('channelId', request.channelId.toString());
    }

    const response = await apiClient.post<FileMetadata>(
      API_CONFIG.ENDPOINTS.FILES.UPLOAD,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      } as AxiosRequestConfig
    );
    return response.data;
  },

  /**
   * Get channel files
   */
  getChannelFiles: async (channelId: number): Promise<FileMetadata[]> => {
    const response = await apiClient.get<FileMetadata[]>(
      API_CONFIG.ENDPOINTS.FILES.CHANNEL_FILES(channelId)
    );
    return response.data;
  },

  /**
   * Download file
   */
  downloadFile: async (fileId: number): Promise<Blob> => {
    const response = await apiClient.get<Blob>(
      API_CONFIG.ENDPOINTS.FILES.DOWNLOAD(fileId),
      {
        responseType: 'blob',
      }
    );
    return response.data;
  },

  /**
   * Lock file
   */
  lockFile: async (fileId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.FILES.LOCK(fileId));
  },

  /**
   * Unlock file
   */
  unlockFile: async (fileId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.FILES.UNLOCK(fileId));
  },
};

