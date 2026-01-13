/**
 * WebSocket Service
 * Handles WebSocket connections for real-time chat and notifications
 */

import { API_CONFIG, getToken, decodeJWT } from '../config/api';
import type { ChatEvent, NotificationEvent, Message, WebRTCSignalingMessage } from '../types/api';

/**
 * WebSocket connection manager
 */
class WebSocketManager {
  private chatConnections: Map<number, WebSocket> = new Map(); // channelId/userId -> WebSocket
  private notificationConnection: WebSocket | null = null;
  private meetingConnections: Map<number, WebSocket> = new Map(); // meetingId -> WebSocket
  private chatCallbacks: Map<number, ((event: ChatEvent) => void)[]> = new Map();
  private notificationCallbacks: ((event: NotificationEvent) => void)[] = [];
  private meetingCallbacks: Map<number, ((message: WebRTCSignalingMessage) => void)[]> = new Map();

  /**
   * Connect to chat WebSocket (channel or direct)
   */
  connectToChat(roomId: number, roomType: 'channel' | 'direct', onMessage: (event: ChatEvent) => void): void {
    // Close existing connection if any
    this.disconnectFromChat(roomId, roomType);

    const token = getToken();
    if (!token) {
      console.error('No token available for WebSocket connection');
      return;
    }

    const userId = decodeJWT(token)?.userId;
    if (!userId) {
      console.error('Invalid token - cannot extract userId');
      return;
    }

    // Build WebSocket URL
    const wsUrl = roomType === 'channel'
      ? `${API_CONFIG.WS_BASE_URL}/ws/chat?token=${token}&channelId=${roomId}`
      : `${API_CONFIG.WS_BASE_URL}/ws/chat?token=${token}&userId=${userId}&directUserId=${roomId}`;

    try {
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log(`WebSocket connected for ${roomType} ${roomId}`);
        
        // Subscribe to topic
        const topic = roomType === 'channel'
          ? API_CONFIG.WS_TOPICS.CHAT_CHANNEL(roomId)
          : API_CONFIG.WS_TOPICS.CHAT_USER(userId);

        ws.send(JSON.stringify({
          type: 'SUBSCRIBE',
          destination: topic,
        }));
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          // Handle different message formats
          if (data.type === 'MESSAGE') {
            onMessage({
              type: 'MESSAGE_RECEIVED',
              message: data.payload as Message,
              chatRoomId: roomId,
            });
          } else if (data.message) {
            // Direct message object
            onMessage({
              type: 'MESSAGE_RECEIVED',
              message: data.message as Message,
              chatRoomId: roomId,
            });
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onerror = (error) => {
        console.error(`WebSocket error for ${roomType} ${roomId}:`, error);
      };

      ws.onclose = () => {
        console.log(`WebSocket closed for ${roomType} ${roomId}`);
        this.chatConnections.delete(roomId);
      };

      this.chatConnections.set(roomId, ws);
      
      // Store callback
      if (!this.chatCallbacks.has(roomId)) {
        this.chatCallbacks.set(roomId, []);
      }
      this.chatCallbacks.get(roomId)!.push(onMessage);
    } catch (error) {
      console.error(`Error connecting to chat WebSocket:`, error);
    }
  }

  /**
   * Disconnect from chat WebSocket
   */
  disconnectFromChat(roomId: number, roomType: 'channel' | 'direct'): void {
    const ws = this.chatConnections.get(roomId);
    if (ws) {
      ws.close();
      this.chatConnections.delete(roomId);
      this.chatCallbacks.delete(roomId);
    }
  }

  /**
   * Connect to notifications WebSocket
   */
  connectToNotifications(onNotification: (event: NotificationEvent) => void): void {
    // Close existing connection if any
    this.disconnectFromNotifications();

    const token = getToken();
    if (!token) {
      console.error('No token available for WebSocket connection');
      return;
    }

    const userId = decodeJWT(token)?.userId;
    if (!userId) {
      console.error('Invalid token - cannot extract userId');
      return;
    }

    const wsUrl = `${API_CONFIG.WS_BASE_URL}/ws/notifications?token=${token}&userId=${userId}`;

    try {
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log('Notifications WebSocket connected');
        
        // Subscribe to notifications topic
        const topic = API_CONFIG.WS_TOPICS.NOTIFICATIONS(userId);
        ws.send(JSON.stringify({
          type: 'SUBSCRIBE',
          destination: topic,
        }));
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          // Handle notification message
          if (data.type === 'NOTIFICATION') {
            onNotification({
              type: 'NOTIFICATION_RECEIVED',
              notification: data.payload || data,
            });
          } else if (data.id) {
            // Direct notification object
            onNotification({
              type: 'NOTIFICATION_RECEIVED',
              notification: data,
            });
          }
        } catch (error) {
          console.error('Error parsing notification message:', error);
        }
      };

      ws.onerror = (error) => {
        // Don't log as error if WebSocket endpoint doesn't exist (404)
        // This is expected if WebSocket support isn't implemented yet
        console.warn('Notifications WebSocket error (endpoint may not be available):', error);
      };

      ws.onclose = (event) => {
        // Only log if it wasn't a normal closure or if code indicates an issue
        if (event.code !== 1000 && event.code !== 1001) {
          console.warn('Notifications WebSocket closed:', event.code, event.reason);
        }
        this.notificationConnection = null;
      };

      this.notificationConnection = ws;
      this.notificationCallbacks.push(onNotification);
    } catch (error) {
      console.error('Error connecting to notifications WebSocket:', error);
    }
  }

  /**
   * Disconnect from notifications WebSocket
   */
  disconnectFromNotifications(): void {
    if (this.notificationConnection) {
      this.notificationConnection.close();
      this.notificationConnection = null;
      this.notificationCallbacks = [];
    }
  }

  /**
   * Connect to meeting WebSocket for signaling
   */
  connectToMeeting(
    meetingId: number,
    userId: number,
    onSignalingMessage: (message: WebRTCSignalingMessage) => void
  ): void {
    // Close existing connection if any
    this.disconnectFromMeeting(meetingId);

    const token = getToken();
    if (!token) {
      console.error('No token available for WebSocket connection');
      return;
    }

    const wsUrl = `${API_CONFIG.WS_BASE_URL}/ws/meeting?token=${token}&meetingId=${meetingId}&userId=${userId}`;

    try {
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log(`Meeting WebSocket connected for meeting ${meetingId}`);
        
        // Subscribe to meeting signaling topic
        const topic = API_CONFIG.WS_TOPICS.MEETING_SIGNALING(meetingId);
        ws.send(JSON.stringify({
          type: 'SUBSCRIBE',
          destination: topic,
        }));
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          // Handle different message formats (backend may wrap in payload or send directly)
          let messageData = data;
          if (data.payload) {
            messageData = typeof data.payload === 'string' ? JSON.parse(data.payload) : data.payload;
          } else if (data.body) {
            messageData = typeof data.body === 'string' ? JSON.parse(data.body) : data.body;
          }
          
          // Handle signaling messages
          if (
            messageData.type === 'OFFER' ||
            messageData.type === 'ANSWER' ||
            messageData.type === 'ICE_CANDIDATE' ||
            messageData.type === 'USER_JOINED' ||
            messageData.type === 'USER_LEFT'
          ) {
            // Normalize message format
            const signalingMessage: WebRTCSignalingMessage = {
              type: messageData.type,
              meetingId: messageData.meetingId || meetingId,
              senderId: messageData.senderId || messageData.fromUserId || messageData.userId,
              fromUserId: messageData.senderId || messageData.fromUserId || messageData.userId,
              targetUserId: messageData.targetUserId || messageData.toUserId,
              toUserId: messageData.targetUserId || messageData.toUserId,
              // Handle nested data object or direct properties
              data: messageData.data || {
                offer: messageData.offer,
                answer: messageData.answer,
                candidate: messageData.candidate || messageData.iceCandidate,
              },
              // Direct properties for backward compatibility
              offer: messageData.data?.offer || messageData.offer,
              answer: messageData.data?.answer || messageData.answer,
              iceCandidate: messageData.data?.candidate || messageData.candidate || messageData.iceCandidate,
              candidate: messageData.data?.candidate || messageData.candidate || messageData.iceCandidate,
            };
            onSignalingMessage(signalingMessage);
          }
        } catch (error) {
          console.error('Error parsing meeting WebSocket message:', error);
        }
      };

      ws.onerror = (error) => {
        console.error(`Meeting WebSocket error for meeting ${meetingId}:`, error);
      };

      ws.onclose = () => {
        console.log(`Meeting WebSocket closed for meeting ${meetingId}`);
        this.meetingConnections.delete(meetingId);
        this.meetingCallbacks.delete(meetingId);
      };

      this.meetingConnections.set(meetingId, ws);
      
      // Store callback
      if (!this.meetingCallbacks.has(meetingId)) {
        this.meetingCallbacks.set(meetingId, []);
      }
      this.meetingCallbacks.get(meetingId)!.push(onSignalingMessage);
    } catch (error) {
      console.error(`Error connecting to meeting WebSocket:`, error);
    }
  }

  /**
   * Send signaling message via meeting WebSocket
   * WebSocket payload format: { type, meetingId, senderId, targetUserId, data }
   */
  sendMeetingSignaling(meetingId: number, message: WebRTCSignalingMessage): void {
    const ws = this.meetingConnections.get(meetingId);
    if (ws && ws.readyState === WebSocket.OPEN) {
      const topic = API_CONFIG.WS_TOPICS.MEETING_SIGNALING(meetingId);
      
      // Format message according to spec: { type, meetingId, senderId, targetUserId, data }
      const formattedMessage = {
        type: message.type,
        meetingId: message.meetingId,
        senderId: message.senderId || message.fromUserId,
        targetUserId: message.targetUserId || message.toUserId,
        data: message.data || {
          offer: message.offer,
          answer: message.answer,
          candidate: message.candidate || message.iceCandidate,
        },
      };

      // Try different message formats depending on backend expectations
      // Format 1: STOMP-like format with destination
      try {
        ws.send(
          JSON.stringify({
            type: 'SEND',
            destination: topic,
            body: JSON.stringify(formattedMessage),
          })
        );
      } catch (error) {
        // Format 2: Direct message (fallback)
        try {
          ws.send(JSON.stringify(formattedMessage));
        } catch (err) {
          console.error('Error sending signaling message:', err);
        }
      }
    } else {
      console.warn(`Meeting WebSocket not connected for meeting ${meetingId}`);
    }
  }

  /**
   * Disconnect from meeting WebSocket
   */
  disconnectFromMeeting(meetingId: number): void {
    const ws = this.meetingConnections.get(meetingId);
    if (ws) {
      ws.close();
      this.meetingConnections.delete(meetingId);
      this.meetingCallbacks.delete(meetingId);
    }
  }

  /**
   * Close all connections
   */
  disconnectAll(): void {
    // Close all chat connections
    this.chatConnections.forEach((ws) => ws.close());
    this.chatConnections.clear();
    this.chatCallbacks.clear();

    // Close notification connection
    this.disconnectFromNotifications();

    // Close all meeting connections
    this.meetingConnections.forEach((ws) => ws.close());
    this.meetingConnections.clear();
    this.meetingCallbacks.clear();
  }
}

// Export singleton instance
export const wsManager = new WebSocketManager();

