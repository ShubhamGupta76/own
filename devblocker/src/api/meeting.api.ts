/**
 * Meeting API Service
 * Handles meeting and video call endpoints
 */

import { apiClient } from './index';
import { API_CONFIG } from '../config/api';
import type {
  Meeting,
  CreateInstantCallRequest,
  ScheduleMeetingRequest,
  MeetingNote,
} from '../types/api';

export const meetingsApi = {
  /**
   * Create instant call
   */
  createInstantCall: async (request: CreateInstantCallRequest): Promise<Meeting> => {
    const response = await apiClient.post<Meeting>(
      API_CONFIG.ENDPOINTS.MEETINGS.INSTANT,
      request
    );
    return response.data;
  },

  /**
   * Schedule meeting
   */
  scheduleMeeting: async (request: ScheduleMeetingRequest): Promise<Meeting> => {
    const response = await apiClient.post<Meeting>(
      API_CONFIG.ENDPOINTS.MEETINGS.SCHEDULE,
      request
    );
    return response.data;
  },

  /**
   * Get meeting by ID
   */
  getMeetingById: async (meetingId: number): Promise<Meeting> => {
    const response = await apiClient.get<Meeting>(`/meetings/${meetingId}`);
    return response.data;
  },

  /**
   * Join meeting
   */
  joinMeeting: async (meetingId: number): Promise<{ participantIds: number[] }> => {
    const response = await apiClient.post<{ participantIds: number[] }>(
      API_CONFIG.ENDPOINTS.MEETINGS.JOIN(meetingId)
    );
    return response.data;
  },

  /**
   * Leave meeting
   */
  leaveMeeting: async (meetingId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.MEETINGS.LEAVE(meetingId));
  },

  /**
   * Start screen share
   */
  startScreenShare: async (meetingId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.MEETINGS.SCREEN_SHARE_START(meetingId));
  },

  /**
   * Stop screen share
   */
  stopScreenShare: async (meetingId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.MEETINGS.SCREEN_SHARE_STOP(meetingId));
  },

  /**
   * Start recording
   */
  startRecording: async (meetingId: number): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.MEETINGS.RECORDING_START(meetingId));
  },

  /**
   * Stop recording
   */
  stopRecording: async (meetingId: number, recordingUrl: string): Promise<void> => {
    await apiClient.post(API_CONFIG.ENDPOINTS.MEETINGS.RECORDING_STOP(meetingId), {
      recordingUrl,
    });
  },

  /**
   * Add meeting note
   */
  addNote: async (meetingId: number, content: string): Promise<MeetingNote> => {
    const response = await apiClient.post<MeetingNote>(
      API_CONFIG.ENDPOINTS.MEETINGS.NOTES(meetingId),
      { content }
    );
    return response.data;
  },

  /**
   * Get meeting notes
   */
  getNotes: async (meetingId: number): Promise<MeetingNote[]> => {
    const response = await apiClient.get<MeetingNote[]>(
      API_CONFIG.ENDPOINTS.MEETINGS.NOTES(meetingId)
    );
    return response.data;
  },
};

