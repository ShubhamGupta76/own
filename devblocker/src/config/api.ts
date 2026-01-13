/**
 * API Configuration
 * Centralized configuration for API Gateway
 */

// Get API Gateway URL from environment or use default
const API_GATEWAY_PORT = import.meta.env.VITE_API_GATEWAY_PORT || '8080';
const API_GATEWAY_HOST = import.meta.env.VITE_API_GATEWAY_HOST || 'localhost';

export const API_CONFIG = {
  BASE_URL: `http://${API_GATEWAY_HOST}:${API_GATEWAY_PORT}`,
  WS_BASE_URL: `ws://${API_GATEWAY_HOST}:${API_GATEWAY_PORT}`,
  
  // API Endpoints
  ENDPOINTS: {
    // Auth
    AUTH: {
      REGISTER: '/auth/register',
      REGISTER_ORGANIZATION: '/auth/register/organization',
      LOGIN: '/auth/login',
      EMPLOYEE_LOGIN: '/auth/employee/login',
      ORGANIZATION_LOGIN: '/auth/login', // Use admin login endpoint
    },
    
    // User
    USER: {
      PROFILE: '/users/profile', // Note: This endpoint doesn't exist - use USER_BY_ID with userId from token
      USER_BY_ID: (userId: number) => `/users/${userId}`,
      USERS: '/users',
    },
    
    // Teams
    TEAMS: {
      LIST: '/teams',
      MY_TEAMS: '/teams/my',
      CREATE: '/teams',
      ADD_MEMBER: (teamId: number) => `/teams/${teamId}/members`,
      REMOVE_MEMBER: (teamId: number, userId: number) => `/teams/${teamId}/members/${userId}`,
    },
    
    // Channels
    CHANNELS: {
      BY_TEAM: (teamId: number) => `/channels/teams/${teamId}/channels`,
      CREATE: (teamId: number) => `/channels/teams/${teamId}/channels`,
      MEMBERS: (channelId: number) => `/channels/channels/${channelId}/members`,
      ADD_MEMBER: (channelId: number) => `/channels/channels/${channelId}/members`,
      REMOVE_MEMBER: (channelId: number, userId: number) => `/channels/channels/${channelId}/members/${userId}`,
    },
    
    // Chat
    CHAT: {
      SEND: '/chat/send',
      CHANNEL_MESSAGES: (channelId: number) => `/chat/channel/${channelId}`,
      DIRECT_MESSAGES: (userId: number) => `/chat/direct/${userId}`,
    },
    
    // Meetings
    MEETINGS: {
      INSTANT: '/meetings/instant',
      SCHEDULE: '/meetings/schedule',
      JOIN: (meetingId: number) => `/meetings/${meetingId}/join`,
      LEAVE: (meetingId: number) => `/meetings/${meetingId}/leave`,
      SCREEN_SHARE_START: (meetingId: number) => `/meetings/${meetingId}/screen-share/start`,
      SCREEN_SHARE_STOP: (meetingId: number) => `/meetings/${meetingId}/screen-share/stop`,
      RECORDING_START: (meetingId: number) => `/meetings/${meetingId}/recording/start`,
      RECORDING_STOP: (meetingId: number) => `/meetings/${meetingId}/recording/stop`,
      NOTES: (meetingId: number) => `/meetings/${meetingId}/notes`,
    },
    
    // Files
    FILES: {
      UPLOAD: '/files/upload',
      CHANNEL_FILES: (channelId: number) => `/files/channel/${channelId}`,
      DOWNLOAD: (fileId: number) => `/files/${fileId}/download`,
      LOCK: (fileId: number) => `/files/${fileId}/lock`,
      UNLOCK: (fileId: number) => `/files/${fileId}/unlock`,
    },
    
    // Tasks
    TASKS: {
      CREATE: '/tasks',
      ASSIGN: (taskId: number) => `/tasks/${taskId}/assign`,
      UPDATE_STATUS: (taskId: number) => `/tasks/${taskId}/status`,
      COMMENT: (taskId: number) => `/tasks/${taskId}/comments`,
      BY_CHANNEL: (channelId: number) => `/tasks/channel/${channelId}`,
      BY_ID: (taskId: number) => `/tasks/${taskId}`,
    },
    
    // Notifications
    NOTIFICATIONS: {
      LIST: '/notifications',
      UNREAD: '/notifications/unread',
      UNREAD_COUNT: '/notifications/unread/count',
      MARK_READ: (notificationId: number) => `/notifications/${notificationId}/read`,
      MARK_ALL_READ: '/notifications/read-all',
      ACTIVITY: '/notifications/activity',
    },
  },
  
  // WebSocket Topics
  WS_TOPICS: {
    CHAT_CHANNEL: (channelId: number) => `/topic/channel/${channelId}`,
    CHAT_USER: (userId: number) => `/topic/user/${userId}`,
    NOTIFICATIONS: (userId: number) => `/topic/notifications/${userId}`,
    MEETING: (meetingId: number) => `/topic/meeting/${meetingId}`,
    MEETING_SIGNALING: (meetingId: number) => `/topic/meeting/${meetingId}/signaling`,
  },
  
  // ICE Servers (STUN/TURN)
  // Default: Google STUN (free, public)
  // Optional: Metered.ca TURN (from env vars)
  ICE_SERVERS: (() => {
    const servers: RTCIceServer[] = [
      // Default Google STUN
      {
        urls: 'stun:stun.l.google.com:19302',
      },
    ];

    // Optional Metered.ca TURN (if credentials provided via env)
    const meteredUsername = import.meta.env.VITE_TURN_USERNAME;
    const meteredCredential = import.meta.env.VITE_TURN_CREDENTIAL;

    if (meteredUsername && meteredCredential) {
      servers.push(
        {
          urls: 'stun:stun.metered.ca:80',
        },
        {
          urls: 'turn:turn.metered.ca:80',
          username: meteredUsername,
          credential: meteredCredential,
        },
        {
          urls: 'turn:turn.metered.ca:443',
          username: meteredUsername,
          credential: meteredCredential,
        },
        {
          urls: 'turn:turn.metered.ca:443?transport=tcp',
          username: meteredUsername,
          credential: meteredCredential,
        }
      );
    }

    return servers;
  })(),
};

/**
 * Get JWT token from localStorage
 */
export const getToken = (): string | null => {
  return localStorage.getItem('jwt_token');
};

/**
 * Set JWT token in localStorage
 */
export const setToken = (token: string): void => {
  localStorage.setItem('jwt_token', token);
};

/**
 * Remove JWT token from localStorage
 */
export const removeToken = (): void => {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user_id');
  localStorage.removeItem('organization_id');
  localStorage.removeItem('user_email');
  localStorage.removeItem('user_role');
};

/**
 * Decode JWT token (simple base64 decode)
 */
export const decodeJWT = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
};

/**
 * Check if token is expired
 */
export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) return true;
  return Date.now() >= decoded.exp * 1000;
};
