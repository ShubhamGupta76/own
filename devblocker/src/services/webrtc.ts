/**
 * WebRTC Service
 * Handles WebRTC peer connections for video/audio calls
 * Uses mesh model (peer-to-peer connections)
 */

import { API_CONFIG } from '../config/api';
import type { WebRTCSignalingMessage, MeetingParticipant } from '../types/api';

/**
 * WebRTC Connection Manager
 * Manages peer connections for meeting participants
 */
export class WebRTCManager {
  private peerConnections: Map<number, RTCPeerConnection> = new Map(); // userId -> RTCPeerConnection
  private localStream: MediaStream | null = null;
  private screenStream: MediaStream | null = null;
  private remoteStreams: Map<number, MediaStream> = new Map(); // userId -> MediaStream
  private isMuted: boolean = false;
  private isVideoEnabled: boolean = true;
  private isScreenSharing: boolean = false;
  private currentMeetingId: number | null = null;
  private currentUserId: number | null = null;
  private sendSignalingCallback: ((message: WebRTCSignalingMessage) => void) | null = null;

  /**
   * Get configuration for RTCPeerConnection
   */
  private getRTCPeerConnectionConfig(): RTCConfiguration {
    return {
      iceServers: API_CONFIG.ICE_SERVERS,
      iceCandidatePoolSize: 10,
    };
  }

  /**
   * Initialize local media stream (camera + microphone)
   */
  async initializeLocalStream(enableVideo: boolean = true, enableAudio: boolean = true): Promise<MediaStream> {
    try {
      const constraints: MediaStreamConstraints = {
        video: enableVideo ? {
          width: { ideal: 1280 },
          height: { ideal: 720 },
        } : false,
        audio: enableAudio,
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      this.localStream = stream;
      this.isVideoEnabled = enableVideo;
      this.isMuted = !enableAudio;

      return stream;
    } catch (error) {
      console.error('Error accessing media devices:', error);
      throw new Error('Failed to access camera/microphone. Please check permissions.');
    }
  }

  /**
   * Stop local media stream
   */
  stopLocalStream(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop());
      this.localStream = null;
    }
    if (this.screenStream) {
      this.screenStream.getTracks().forEach((track) => track.stop());
      this.screenStream = null;
    }
    this.isScreenSharing = false;
  }

  /**
   * Toggle microphone mute/unmute
   */
  toggleMute(): boolean {
    if (!this.localStream) return this.isMuted;

    const audioTracks = this.localStream.getAudioTracks();
    audioTracks.forEach((track) => {
      track.enabled = this.isMuted;
    });

    this.isMuted = !this.isMuted;
    return this.isMuted;
  }

  /**
   * Toggle video on/off
   */
  toggleVideo(): boolean {
    if (!this.localStream) return !this.isVideoEnabled;

    const videoTracks = this.localStream.getVideoTracks();
    videoTracks.forEach((track) => {
      track.enabled = !this.isVideoEnabled;
    });

    this.isVideoEnabled = !this.isVideoEnabled;
    return this.isVideoEnabled;
  }

  /**
   * Start screen sharing
   */
  async startScreenShare(): Promise<MediaStream> {
    try {
      if (this.screenStream) {
        // Already sharing, stop first
        await this.stopScreenShare();
      }

      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          displaySurface: 'monitor',
          width: { ideal: 1920 },
          height: { ideal: 1080 },
        } as MediaTrackConstraints,
        audio: true,
      });

      this.screenStream = stream;
      this.isScreenSharing = true;

      // Replace video track in all peer connections
      const videoTrack = stream.getVideoTracks()[0];
      this.peerConnections.forEach((pc) => {
        const sender = pc.getSenders().find((s) => s.track?.kind === 'video');
        if (sender && videoTrack) {
          sender.replaceTrack(videoTrack);
        }
      });

      // Handle screen share stop (user clicks stop sharing in browser)
      stream.getVideoTracks()[0].onended = () => {
        this.stopScreenShare();
      };

      return stream;
    } catch (error) {
      console.error('Error starting screen share:', error);
      throw new Error('Failed to start screen sharing. Please check permissions.');
    }
  }

  /**
   * Stop screen sharing
   */
  async stopScreenShare(): Promise<void> {
    if (this.screenStream) {
      this.screenStream.getTracks().forEach((track) => track.stop());
      this.screenStream = null;
    }

    this.isScreenSharing = false;

    // Restore camera video track
    if (this.localStream && this.isVideoEnabled) {
      const videoTrack = this.localStream.getVideoTracks()[0];
      if (videoTrack) {
        this.peerConnections.forEach((pc) => {
          const sender = pc.getSenders().find((s) => s.track?.kind === 'video');
          if (sender && videoTrack) {
            sender.replaceTrack(videoTrack);
          }
        });
      }
    }
  }

  /**
   * Create or get peer connection for a user
   */
  private getOrCreatePeerConnection(userId: number): RTCPeerConnection {
    if (this.peerConnections.has(userId)) {
      return this.peerConnections.get(userId)!;
    }

    const pc = new RTCPeerConnection(this.getRTCPeerConnectionConfig());

    // Add local tracks to peer connection
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => {
        pc.addTrack(track, this.localStream!);
      });
    }

    // Handle remote tracks
    pc.ontrack = (event) => {
      const [remoteStream] = event.streams;
      if (remoteStream) {
        this.remoteStreams.set(userId, remoteStream);
        // Notify listeners of new remote stream
        this.notifyRemoteStream(userId, remoteStream);
      }
    };

    // Handle ICE candidates
    pc.onicecandidate = (event) => {
      if (event.candidate && this.currentMeetingId && this.currentUserId && this.sendSignalingCallback) {
        this.sendSignalingCallback({
          type: 'ICE_CANDIDATE',
          meetingId: this.currentMeetingId,
          senderId: this.currentUserId,
          fromUserId: this.currentUserId,
          targetUserId: userId,
          toUserId: userId,
          iceCandidate: event.candidate.toJSON(),
        });
      }
    };

    // Handle connection state changes
    pc.onconnectionstatechange = () => {
      console.log(`Peer connection with user ${userId}: ${pc.connectionState}`);
      if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
        // Attempt to reconnect
        this.handlePeerConnectionFailure(userId);
      }
    };

    this.peerConnections.set(userId, pc);
    return pc;
  }

  /**
   * Handle peer connection failure (attempt reconnect)
   */
  private async handlePeerConnectionFailure(userId: number): Promise<void> {
    const pc = this.peerConnections.get(userId);
    if (!pc) return;

    if (pc.connectionState === 'failed') {
      // Try to restart ICE
      try {
        await pc.restartIce();
      } catch (error) {
        console.error(`Failed to restart ICE for user ${userId}:`, error);
        // Remove failed connection
        this.closePeerConnection(userId);
      }
    }
  }

  /**
   * Close peer connection for a user
   */
  private closePeerConnection(userId: number): void {
    const pc = this.peerConnections.get(userId);
    if (pc) {
      pc.close();
      this.peerConnections.delete(userId);
      this.remoteStreams.delete(userId);
    }
  }

  /**
   * Join meeting and establish connections with other participants
   */
  async joinMeeting(
    meetingId: number,
    userId: number,
    participantIds: number[],
    sendSignaling: (message: WebRTCSignalingMessage) => void
  ): Promise<void> {
    this.currentMeetingId = meetingId;
    this.currentUserId = userId;
    this.sendSignalingCallback = sendSignaling;

    // Initialize local stream
    if (!this.localStream) {
      await this.initializeLocalStream(true, true);
    }

    // Create peer connections for existing participants
    for (const participantId of participantIds) {
      if (participantId !== userId) {
        const pc = this.getOrCreatePeerConnection(participantId);
        
        // Create and send offer
        try {
          const offer = await pc.createOffer();
          await pc.setLocalDescription(offer);

          sendSignaling({
            type: 'OFFER',
            meetingId,
            senderId: userId,
            fromUserId: userId,
            targetUserId: participantId,
            toUserId: participantId,
            offer: offer,
          });
        } catch (error) {
          console.error(`Error creating offer for user ${participantId}:`, error);
        }
      }
    }

    // Send leave notification
    if (this.currentMeetingId && this.currentUserId && this.sendSignalingCallback) {
      this.sendSignalingCallback({
        type: 'LEAVE_MEETING',
        meetingId: this.currentMeetingId,
        senderId: this.currentUserId,
        fromUserId: this.currentUserId,
      });
    }

    // Close all peer connections
    this.peerConnections.forEach((pc, userId) => {
      pc.close();
    });
    this.peerConnections.clear();
    this.remoteStreams.clear();

    // Stop local streams
    this.stopLocalStream();

    // Reset state
    this.currentMeetingId = null;
    this.currentUserId = null;
    this.sendSignalingCallback = null;
  }

  /**
   * Handle incoming signaling message
   */
  async handleSignalingMessage(message: WebRTCSignalingMessage): Promise<void> {
    if (!this.currentMeetingId || !this.currentUserId) {
      return;
    }

    const { type, fromUserId, offer, answer, iceCandidate } = message;

    // Ignore messages from self
    if (fromUserId === this.currentUserId) {
      return;
    }

    const pc = this.getOrCreatePeerConnection(fromUserId);

    switch (type) {
      case 'OFFER':
        if (offer) {
          try {
            await pc.setRemoteDescription(new RTCSessionDescription(offer));
            const answer = await pc.createAnswer();
            await pc.setLocalDescription(answer);

            if (this.sendSignalingCallback) {
              this.sendSignalingCallback({
                type: 'ANSWER',
                meetingId: this.currentMeetingId,
                senderId: this.currentUserId,
                fromUserId: this.currentUserId,
                targetUserId: fromUserId,
                toUserId: fromUserId,
                answer: answer,
              });
            }
          } catch (error) {
            console.error('Error handling OFFER:', error);
          }
        }
        break;

      case 'ANSWER':
        if (answer) {
          try {
            await pc.setRemoteDescription(new RTCSessionDescription(answer));
          } catch (error) {
            console.error('Error handling ANSWER:', error);
          }
        }
        break;

      case 'ICE_CANDIDATE':
        if (iceCandidate) {
          try {
            await pc.addIceCandidate(new RTCIceCandidate(iceCandidate));
          } catch (error) {
            console.error('Error handling ICE_CANDIDATE:', error);
          }
        }
        break;
    }
  }


  /**
   * Get local stream
   */
  getLocalStream(): MediaStream | null {
    return this.localStream;
  }

  /**
   * Get remote stream for a user
   */
  getRemoteStream(userId: number): MediaStream | null {
    return this.remoteStreams.get(userId) || null;
  }

  /**
   * Get all remote streams
   */
  getAllRemoteStreams(): Map<number, MediaStream> {
    return new Map(this.remoteStreams);
  }

  /**
   * Get connection state
   */
  getState() {
    return {
      isMuted: this.isMuted,
      isVideoEnabled: this.isVideoEnabled,
      isScreenSharing: this.isScreenSharing,
      currentMeetingId: this.currentMeetingId,
      participantCount: this.peerConnections.size,
    };
  }

  /**
   * Callback for remote stream updates
   */
  private remoteStreamCallbacks: Map<number, ((stream: MediaStream) => void)[]> = new Map();

  /**
   * Register callback for remote stream updates
   */
  onRemoteStream(userId: number, callback: (stream: MediaStream) => void): void {
    if (!this.remoteStreamCallbacks.has(userId)) {
      this.remoteStreamCallbacks.set(userId, []);
    }
    this.remoteStreamCallbacks.get(userId)!.push(callback);

    // If stream already exists, call immediately
    const existingStream = this.remoteStreams.get(userId);
    if (existingStream) {
      callback(existingStream);
    }
  }

  /**
   * Unregister callback for remote stream updates
   */
  offRemoteStream(userId: number, callback: (stream: MediaStream) => void): void {
    const callbacks = this.remoteStreamCallbacks.get(userId);
    if (callbacks) {
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  /**
   * Notify listeners of new remote stream
   */
  private notifyRemoteStream(userId: number, stream: MediaStream): void {
    const callbacks = this.remoteStreamCallbacks.get(userId) || [];
    callbacks.forEach((callback) => callback(stream));
  }
}

// Export singleton instance
export const webrtcManager = new WebRTCManager();

