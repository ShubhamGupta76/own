/**
 * TypeScript interfaces matching DevBlocker backend API contracts
 */

// User & Auth Types
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE' | 'EXTERNAL_USER';

export interface LoginResponse {
  token: string;
  userId: number;
  email: string;
  role: UserRole;
  organizationId: number | null;
  isFirstLogin?: boolean;
  profileSetupRequired?: boolean;
}

export interface User {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  role: UserRole;
  organizationId: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  lastLoginAt?: string;
  isFirstLogin?: boolean;
  profileSetupRequired?: boolean;
}

export interface JWTPayload {
  userId: number;
  email: string;
  role: UserRole;
  organizationId: number;
  iat?: number;
  exp?: number;
}

export interface OrganizationRegistrationRequest {
  organizationName: string;
  adminEmail: string;
  adminPassword: string;
  adminFirstName: string;
  adminLastName: string;
  description?: string;
}

// Team & Channel Types
export interface Team {
  id: number;
  name: string;
  description?: string;
  organizationId: number;
  createdBy: number;
  createdAt: string;
  updatedAt?: string;
}

export interface Channel {
  id: number;
  name: string;
  description?: string;
  teamId: number;
  type: 'STANDARD' | 'PRIVATE';
  organizationId: number;
  createdBy: number;
  createdAt: string;
  updatedAt?: string;
}

// Chat & Message Types
export interface ChatRoom {
  id: number;
  roomType: 'CHANNEL' | 'TEAM' | 'DIRECT';
  roomId: number; // channelId, teamId, or userId
  organizationId: number;
  createdAt: string;
}

export interface Message {
  id: number;
  chatRoomId: number;
  senderId: number;
  senderRole: string;
  content: string;
  messageType: 'TEXT' | 'FILE' | 'LINK' | 'EMOJI' | 'GIF' | 'SYSTEM';
  fileId?: number;
  metadata?: Record<string, any>;
  organizationId: number;
  status: 'SENT' | 'DELIVERED' | 'READ';
  createdAt: string;
  timestamp?: Date | string; // Add timestamp for compatibility
  updatedAt?: string;
}

export interface SendMessageRequest {
  chatRoomId: number;
  content: string;
  messageType?: 'TEXT' | 'FILE' | 'LINK' | 'EMOJI' | 'GIF';
  fileId?: number;
  metadata?: Record<string, any>;
}

// Meeting Types
export interface Meeting {
  id: number;
  title: string;
  description?: string;
  meetingType: 'INSTANT' | 'SCHEDULED';
  status: 'SCHEDULED' | 'LIVE' | 'ENDED';
  startTime?: string;
  endTime?: string;
  scheduledAt?: string;
  meetingUrl?: string;
  teamId?: number;
  channelId?: number;
  createdBy: number;
  organizationId: number;
  createdAt: string;
  endedAt?: string;
}

export interface CreateInstantCallRequest {
  title: string;
  description?: string;
  teamId?: number;
  channelId?: number;
  meetingUrl?: string;
  participantIds: number[];
}

export interface ScheduleMeetingRequest {
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  teamId?: number;
  channelId?: number;
  meetingUrl?: string;
  participantIds: number[];
}

export interface MeetingNote {
  id: number;
  meetingId: number;
  content: string;
  createdBy: number;
  createdAt: string;
}

// File Types
export interface FileMetadata {
  id: number;
  filename: string;
  size: number;
  contentType: string;
  channelId?: number;
  chatMessageId?: number;
  uploadedBy: number;
  organizationId: number;
  version: number;
  lockedBy?: number;
  uploadedAt: string;
}

export interface UploadFileRequest {
  file: File;
  channelId?: number;
}

// Task Types
export interface Task {
  id: number;
  title: string;
  description?: string;
  channelId: number;
  teamId: number;
  taskType: 'TASK' | 'BUG' | 'STORY';
  status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'DONE';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  assignedTo?: number;
  createdBy: number;
  organizationId: number;
  dueDate?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface TaskComment {
  id: number;
  taskId: number;
  content: string;
  createdBy: number;
  createdAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  channelId: number;
  taskType: 'TASK' | 'BUG' | 'STORY';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  assignedTo?: number;
  dueDate?: string;
}

// Notification Types
export interface Notification {
  id: number;
  userId: number;
  organizationId: number;
  type: 'SYSTEM' | 'MENTION' | 'TASK' | 'FILE' | 'MEETING' | 'MESSAGE' | 'ACTIVITY';
  title: string;
  message: string;
  targetEntityType?: 'USER' | 'TEAM' | 'CHANNEL' | 'TASK' | 'FILE' | 'MEETING' | 'MESSAGE';
  targetEntityId?: number;
  targetEntityName?: string;
  sourceId?: number;
  read: boolean;
  createdAt: string;
  timestamp?: Date | string; // Add timestamp for compatibility
}

// WebSocket Event Types
export interface ChatEvent {
  type: 'MESSAGE_RECEIVED' | 'USER_TYPING' | 'USER_STOPPED_TYPING';
  message?: Message;
  chatRoomId?: number;
  userId?: number;
}

export interface NotificationEvent {
  type: 'NOTIFICATION_RECEIVED';
  notification: Notification;
}

// WebRTC Signaling Types
export interface WebRTCSignalingMessage {
  type: 'OFFER' | 'ANSWER' | 'ICE_CANDIDATE' | 'USER_JOINED' | 'USER_LEFT' | 'LEAVE_MEETING';
  meetingId: number;
  senderId: number; // Alias for fromUserId for consistency
  fromUserId: number; // Legacy alias
  targetUserId?: number; // Alias for toUserId for consistency
  toUserId?: number; // Legacy alias - undefined means broadcast to all
  data?: {
    offer?: RTCSessionDescriptionInit;
    answer?: RTCSessionDescriptionInit;
    candidate?: RTCIceCandidateInit;
  };
  // Direct properties (for backward compatibility)
  offer?: RTCSessionDescriptionInit;
  answer?: RTCSessionDescriptionInit;
  iceCandidate?: RTCIceCandidateInit;
  candidate?: RTCIceCandidateInit;
}

export interface MeetingParticipant {
  userId: number;
  email: string;
  displayName?: string;
  isMuted: boolean;
  isVideoEnabled: boolean;
  isScreenSharing: boolean;
  joinedAt: string;
}

// API Response Types
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiError {
  error: string;
  status: number;
  path?: string;
  timestamp?: string;
}

