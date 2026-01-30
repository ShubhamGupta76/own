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
  userId: string;
  email: string;
  role: UserRole;
  organizationId: string | null;
  isFirstLogin?: boolean;
  profileSetupRequired?: boolean;
}

export interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  role: UserRole;
  organizationId: string;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  lastLoginAt?: string;
  isFirstLogin?: boolean;
  profileSetupRequired?: boolean;
}

export interface JWTPayload {
  userId: string;
  email: string;
  role: UserRole;
  organizationId: string;
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

export interface Organization {
  id: string;
  name: string;
  domain?: string;
  adminId: string;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

// Team & Channel Types
export interface Team {
  id: string;
  name: string;
  description?: string;
  organizationId: string;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
}

export interface Channel {
  id: string;
  name: string;
  description?: string;
  teamId: string;
  type: 'STANDARD' | 'PRIVATE';
  organizationId: string;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
}

// Chat & Message Types
export interface ChatRoom {
  id: string;
  roomType: 'CHANNEL' | 'TEAM' | 'DIRECT';
  roomId: string; // channelId, teamId, or userId
  organizationId: string;
  createdAt: string;
}

export interface Message {
  id: string;
  chatRoomId: string;
  senderId: string;
  senderRole: string;
  content: string;
  messageType: 'TEXT' | 'FILE' | 'LINK' | 'EMOJI' | 'GIF' | 'SYSTEM';
  fileId?: string;
  metadata?: Record<string, any>;
  organizationId: string;
  status: 'SENT' | 'DELIVERED' | 'READ';
  createdAt: string;
  timestamp?: Date | string; // Add timestamp for compatibility
  updatedAt?: string;
}

export interface SendMessageRequest {
  chatRoomId: string;
  content: string;
  messageType?: 'TEXT' | 'FILE' | 'LINK' | 'EMOJI' | 'GIF';
  fileId?: string;
  metadata?: Record<string, any>;
}

// Meeting Types
export interface Meeting {
  id: string;
  title: string;
  description?: string;
  meetingType: 'INSTANT' | 'SCHEDULED';
  status: 'SCHEDULED' | 'LIVE' | 'ENDED';
  startTime?: string;
  endTime?: string;
  scheduledAt?: string;
  meetingUrl?: string;
  teamId?: string;
  channelId?: string;
  createdBy: string;
  organizationId: string;
  createdAt: string;
  endedAt?: string;
}

export interface CreateInstantCallRequest {
  title: string;
  description?: string;
  teamId?: string;
  channelId?: string;
  meetingUrl?: string;
  participantIds: string[];
}

export interface ScheduleMeetingRequest {
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  teamId?: string;
  channelId?: string;
  meetingUrl?: string;
  participantIds: string[];
}

export interface MeetingNote {
  id: string;
  meetingId: string;
  content: string;
  createdBy: string;
  createdAt: string;
}

// File Types
export interface FileMetadata {
  id: string;
  filename: string;
  size: number;
  contentType: string;
  channelId?: string;
  chatMessageId?: string;
  uploadedBy: string;
  organizationId: string;
  version: number;
  lockedBy?: string;
  uploadedAt: string;
}

export interface UploadFileRequest {
  file: File;
  channelId?: string;
}

// Task Types
export interface Task {
  id: string;
  title: string;
  description?: string;
  channelId: string;
  teamId: string;
  taskType: 'TASK' | 'BUG' | 'STORY';
  status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'DONE';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  assignedTo?: string;
  createdBy: string;
  organizationId: string;
  dueDate?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface TaskComment {
  id: string;
  taskId: string;
  content: string;
  createdBy: string;
  createdAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  channelId: string;
  taskType: 'TASK' | 'BUG' | 'STORY';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  assignedTo?: string;
  dueDate?: string;
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  organizationId: string;
  type: 'SYSTEM' | 'MENTION' | 'TASK' | 'FILE' | 'MEETING' | 'MESSAGE' | 'ACTIVITY';
  title: string;
  message: string;
  targetEntityType?: 'USER' | 'TEAM' | 'CHANNEL' | 'TASK' | 'FILE' | 'MEETING' | 'MESSAGE';
  targetEntityId?: string;
  targetEntityName?: string;
  sourceId?: string;
  read: boolean;
  createdAt: string;
  timestamp?: Date | string; // Add timestamp for compatibility
}

// WebSocket Event Types
export interface ChatEvent {
  type: 'MESSAGE_RECEIVED' | 'USER_TYPING' | 'USER_STOPPED_TYPING';
  message?: Message;
  chatRoomId?: string;
  userId?: string;
}

export interface NotificationEvent {
  type: 'NOTIFICATION_RECEIVED';
  notification: Notification;
}

// WebRTC Signaling Types
export interface WebRTCSignalingMessage {
  type: 'OFFER' | 'ANSWER' | 'ICE_CANDIDATE' | 'USER_JOINED' | 'USER_LEFT' | 'LEAVE_MEETING';
  meetingId: string;
  senderId: string; // Alias for fromUserId for consistency
  fromUserId: string; // Legacy alias
  targetUserId?: string; // Alias for toUserId for consistency
  toUserId?: string; // Legacy alias - undefined means broadcast to all
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
  userId: string;
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
  errorCode?: string;
  message?: string;
}

