/**
 * WebSocket Service
 * Handles WebSocket connections for real-time chat and notifications
 */

import { Client } from '@stomp/stompjs';
import { API_CONFIG, getToken, decodeJWT } from '../config/api';
import type { ChatEvent, NotificationEvent, Message, WebRTCSignalingMessage } from '../types/api';

/**
 * WebSocket connection manager
 */
class WebSocketManager {
  private chatConnections: Map<number, Client> = new Map(); // channelId/userId -> STOMP Client
  private notificationConnection: Client | null = null;
  private meetingConnections: Map<number, WebSocket> = new Map(); // meetingId -> WebSocket
  private chatCallbacks: Map<number, ((event: ChatEvent) => void)[]> = new Map();
  private notificationCallbacks: ((event: NotificationEvent) => void)[] = [];
  private meetingCallbacks: Map<number, ((message: WebRTCSignalingMessage) => void)[]> = new Map();

  /**
   * Connect to chat WebSocket (channel or direct) using STOMP
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

    // Build WebSocket URL - SockJS endpoint
    const wsUrl = `${API_CONFIG.WS_BASE_URL}/ws/chat?token=${encodeURIComponent(token)}&${roomType === 'channel' ? `channelId=${roomId}` : `userId=${userId}&directUserId=${roomId}`}`;

    try {
      const client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log(`Chat STOMP WebSocket connected for ${roomType} ${roomId}`, frame);
          
          // Subscribe to topic
          const topic = roomType === 'channel'
            ? API_CONFIG.WS_TOPICS.CHAT_CHANNEL(roomId)
            : API_CONFIG.WS_TOPICS.CHAT_USER(userId);
          
          if (client.connected) {
            client.subscribe(topic, (message) => {
              try {
                console.log(`Received WebSocket message on topic ${topic}:`, message.body);
                const data = JSON.parse(message.body);
                
                // Handle different message formats
                let messageObj: Message | null = null;
                
                if (data.type === 'MESSAGE') {
                  messageObj = data.payload as Message;
                } else if (data.message) {
                  messageObj = data.message as Message;
                } else if (data.id || data.content) {
                  messageObj = data as Message;
                }
                
                if (messageObj) {
                  console.log('Parsed message object:', {
                    id: messageObj.id,
                    senderId: messageObj.senderId,
                    chatRoomId: messageObj.chatRoomId,
                    content: messageObj.content?.substring(0, 50)
                  });
                  onMessage({
                    type: 'MESSAGE_RECEIVED',
                    message: messageObj,
                    chatRoomId: roomId,
                  });
                } else {
                  console.warn('Could not parse message object from:', data);
                }
              } catch (error) {
                console.error('Error parsing chat message:', error, message.body);
              }
            });
            console.log(`Subscribed to chat topic: ${topic} for ${roomType} ${roomId}`);
          } else {
            console.warn('Client not connected, cannot subscribe to topic:', topic);
          }
        },
        onStompError: (frame) => {
          console.error(`STOMP error for ${roomType} ${roomId}:`, frame.headers['message'] || frame);
        },
        onWebSocketError: (error) => {
          console.error(`Chat WebSocket error for ${roomType} ${roomId}:`, error);
        },
        onDisconnect: () => {
          console.log(`Chat WebSocket disconnected for ${roomType} ${roomId}`);
          this.chatConnections.delete(roomId);
        },
      });

      // Activate the client
      client.activate();
      
      this.chatConnections.set(roomId, client);
      
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
    const client = this.chatConnections.get(roomId);
    if (client) {
      if (client.connected) {
        client.deactivate();
      }
      this.chatConnections.delete(roomId);
      this.chatCallbacks.delete(roomId);
    }
  }

  /**
   * Connect to notifications WebSocket using STOMP
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

    // Build WebSocket URL - token in query params for initial handshake
    const wsUrl = `${API_CONFIG.WS_BASE_URL}/ws/notifications?token=${encodeURIComponent(token)}&userId=${userId}`;

    try {
      const client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('Notifications STOMP WebSocket connected', frame);
          
          // Subscribe to notifications topic
          const topic = API_CONFIG.WS_TOPICS.NOTIFICATIONS(userId);
          
          if (client.connected) {
            client.subscribe(topic, (message) => {
              try {
                const data = JSON.parse(message.body);
                
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
            });
            console.log(`Subscribed to notifications topic: ${topic}`);
          }
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame.headers['message'] || frame);
        },
        onWebSocketError: (error) => {
          console.warn('Notifications WebSocket error:', error);
        },
        onDisconnect: () => {
          console.log('Notifications WebSocket disconnected');
          this.notificationConnection = null;
        },
      });

      // Activate the client
      client.activate();
      
      this.notificationConnection = client;
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
      if (this.notificationConnection.connected) {
        this.notificationConnection.deactivate();
      }
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
    this.chatConnections.forEach((client) => {
      if (client.connected) {
        client.deactivate();
      }
    });
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

