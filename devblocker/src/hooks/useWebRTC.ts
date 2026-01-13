/**
 * useWebRTC Hook
 * Reusable React hook for WebRTC peer-to-peer video/audio calling
 * Implements mesh model for multiple participants
 */

import { useState, useEffect, useRef, useCallback } from 'react';
import { API_CONFIG } from '../config/api';
import type { WebRTCSignalingMessage } from '../types/api';

interface PeerConnection {
  pc: RTCPeerConnection;
  stream: MediaStream | null;
  userId: number;
}

interface UseWebRTCOptions {
  meetingId: number;
  userId: number;
  onSignalingMessage: (message: WebRTCSignalingMessage) => void;
  onRemoteStream?: (userId: number, stream: MediaStream) => void;
  onUserJoined?: (userId: number) => void;
  onUserLeft?: (userId: number) => void;
  onError?: (error: Error) => void;
}

interface UseWebRTCReturn {
  localStream: MediaStream | null;
  remoteStreams: Map<number, MediaStream>;
  isMuted: boolean;
  isVideoEnabled: boolean;
  isScreenSharing: boolean;
  participants: Set<number>;
  initialize: (enableVideo?: boolean, enableAudio?: boolean) => Promise<void>;
  toggleMute: () => boolean;
  toggleVideo: () => boolean;
  startScreenShare: () => Promise<void>;
  stopScreenShare: () => Promise<void>;
  leave: () => void;
  joinMeeting: (participantIds: number[]) => Promise<void>;
  handleSignalingMessage: (message: WebRTCSignalingMessage) => Promise<void>;
}

/**
 * WebRTC Hook for peer-to-peer video/audio calling
 * Handles peer connections, media streams, and signaling
 */
export const useWebRTC = ({
  meetingId,
  userId,
  onSignalingMessage,
  onRemoteStream,
  onUserJoined,
  onUserLeft,
  onError,
}: UseWebRTCOptions): UseWebRTCReturn => {
  const [localStream, setLocalStream] = useState<MediaStream | null>(null);
  const [remoteStreams, setRemoteStreams] = useState<Map<number, MediaStream>>(new Map());
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [participants, setParticipants] = useState<Set<number>>(new Set());

  const peerConnectionsRef = useRef<Map<number, PeerConnection>>(new Map());
  const screenStreamRef = useRef<MediaStream | null>(null);
  const isInitializedRef = useRef(false);

  /**
   * Get RTC Peer Connection configuration with ICE servers
   */
  const getRTCPeerConnectionConfig = useCallback((): RTCConfiguration => {
    return {
      iceServers: API_CONFIG.ICE_SERVERS,
      iceCandidatePoolSize: 10,
    };
  }, []);

  /**
   * Initialize local media stream (camera + microphone)
   */
  const initialize = useCallback(
    async (enableVideo: boolean = true, enableAudio: boolean = true): Promise<void> => {
      try {
        // Stop existing stream if any
        if (localStream) {
          localStream.getTracks().forEach((track) => track.stop());
        }

        const constraints: MediaStreamConstraints = {
          video: enableVideo
            ? {
                width: { ideal: 1280 },
                height: { ideal: 720 },
              }
            : false,
          audio: enableAudio,
        };

        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        setLocalStream(stream);
        setIsVideoEnabled(enableVideo);
        setIsMuted(!enableAudio);
        isInitializedRef.current = true;

        // Add tracks to all existing peer connections
        peerConnectionsRef.current.forEach(({ pc }) => {
          stream.getTracks().forEach((track) => {
            pc.addTrack(track, stream);
          });
        });
      } catch (error) {
        const err = error instanceof Error ? error : new Error('Failed to access media devices');
        console.error('Error initializing local stream:', err);
        onError?.(err);
        throw err;
      }
    },
    [localStream, onError]
  );

  /**
   * Create or get peer connection for a user
   */
  const getOrCreatePeerConnection = useCallback(
    (targetUserId: number): RTCPeerConnection => {
      const existing = peerConnectionsRef.current.get(targetUserId);
      if (existing?.pc.connectionState !== 'closed') {
        return existing.pc;
      }

      // Remove closed connection
      if (existing) {
        peerConnectionsRef.current.delete(targetUserId);
      }

      // Create new peer connection
      const pc = new RTCPeerConnection(getRTCPeerConnectionConfig());

      // Add local tracks if available
      if (localStream) {
        localStream.getTracks().forEach((track) => {
          pc.addTrack(track, localStream);
        });
      }

      // Handle remote tracks
      pc.ontrack = (event) => {
        const [remoteStream] = event.streams;
        if (remoteStream) {
          setRemoteStreams((prev) => {
            const newMap = new Map(prev);
            newMap.set(targetUserId, remoteStream);
            return newMap;
          });

          // Store stream in peer connection
          const peerConn = peerConnectionsRef.current.get(targetUserId);
          if (peerConn) {
            peerConnectionsRef.current.set(targetUserId, {
              ...peerConn,
              stream: remoteStream,
            });
          }

          onRemoteStream?.(targetUserId, remoteStream);
        }
      };

      // Handle ICE candidates
      pc.onicecandidate = (event) => {
        if (event.candidate) {
          onSignalingMessage({
            type: 'ICE_CANDIDATE',
            meetingId,
            senderId: userId,
            fromUserId: userId,
            targetUserId: targetUserId,
            toUserId: targetUserId,
            data: {
              candidate: event.candidate.toJSON(),
            },
            candidate: event.candidate.toJSON(),
            iceCandidate: event.candidate.toJSON(),
          });
        }
      };

      // Handle connection state changes
      pc.onconnectionstatechange = () => {
        const state = pc.connectionState;
        console.log(`Peer connection with user ${targetUserId}: ${state}`);

        if (state === 'failed' || state === 'disconnected') {
          // Attempt to restart ICE
          pc.restartIce().catch((error) => {
            console.error(`Failed to restart ICE for user ${targetUserId}:`, error);
          });
        }

        if (state === 'closed') {
          // Remove closed connection
          peerConnectionsRef.current.delete(targetUserId);
          setRemoteStreams((prev) => {
            const newMap = new Map(prev);
            newMap.delete(targetUserId);
            return newMap;
          });
        }
      };

      // Store peer connection
      peerConnectionsRef.current.set(targetUserId, {
        pc,
        stream: null,
        userId: targetUserId,
      });

      return pc;
    },
    [localStream, meetingId, userId, getRTCPeerConnectionConfig, onSignalingMessage, onRemoteStream]
  );

  /**
   * Handle incoming signaling message
   */
  const handleSignalingMessage = useCallback(
    async (message: WebRTCSignalingMessage) => {
      // Ignore messages from self
      const senderId = message.senderId || message.fromUserId;
      if (senderId === userId) {
        return;
      }

      const { type, data } = message;
      const fromUserId = senderId;
      const offer = message.offer || data?.offer;
      const answer = message.answer || data?.answer;
      const iceCandidate = message.iceCandidate || message.candidate || data?.candidate;
      
      const pc = getOrCreatePeerConnection(fromUserId);

      try {
        switch (type) {
          case 'OFFER': {
            const offerData = offer || message.data?.offer;
            if (offerData) {
              await pc.setRemoteDescription(new RTCSessionDescription(offerData));
              const answerDesc = await pc.createAnswer();
              await pc.setLocalDescription(answerDesc);

              onSignalingMessage({
                type: 'ANSWER',
                meetingId,
                senderId: userId,
                fromUserId: userId,
                targetUserId: fromUserId,
                toUserId: fromUserId,
                data: {
                  answer: answerDesc,
                },
                answer: answerDesc,
              });
            }
            break;
          }

          case 'ANSWER': {
            const answerData = answer || message.data?.answer;
            if (answerData) {
              await pc.setRemoteDescription(new RTCSessionDescription(answerData));
            }
            break;
          }

          case 'ICE_CANDIDATE': {
            const candidateData = iceCandidate || message.data?.candidate || message.candidate;
            if (candidateData) {
              await pc.addIceCandidate(new RTCIceCandidate(candidateData));
            }
            break;
          }

          case 'USER_JOINED':
            // New participant joined, create offer for them
            setParticipants((prev) => new Set([...prev, fromUserId]));
            onUserJoined?.(fromUserId);

            // Create offer for new participant
            try {
              const offer = await pc.createOffer();
              await pc.setLocalDescription(offer);

              onSignalingMessage({
                type: 'OFFER',
                meetingId,
                senderId: userId,
                fromUserId: userId,
                targetUserId: fromUserId,
                toUserId: fromUserId,
                data: {
                  offer: offer,
                },
                offer: offer,
              });
            } catch (error) {
              console.error('Error creating offer for new participant:', error);
            }
            break;

          case 'USER_LEFT':
            // Participant left, close connection
            setParticipants((prev) => {
              const newSet = new Set(prev);
              newSet.delete(fromUserId);
              return newSet;
            });
            onUserLeft?.(fromUserId);

            const peerConn = peerConnectionsRef.current.get(fromUserId);
            if (peerConn) {
              peerConn.pc.close();
              peerConnectionsRef.current.delete(fromUserId);
            }

            setRemoteStreams((prev) => {
              const newMap = new Map(prev);
              newMap.delete(fromUserId);
              return newMap;
            });
            break;
        }
      } catch (error) {
        console.error(`Error handling ${type}:`, error);
        onError?.(error instanceof Error ? error : new Error(`Failed to handle ${type}`));
      }
    },
    [userId, meetingId, getOrCreatePeerConnection, onSignalingMessage, onUserJoined, onUserLeft, onError]
  );

  /**
   * Join meeting with existing participants
   */
  const joinMeeting = useCallback(
    async (participantIds: number[]) => {
      if (!isInitializedRef.current || !localStream) {
        throw new Error('Local stream not initialized. Call initialize() first.');
      }

      // Create peer connections for existing participants
      for (const participantId of participantIds) {
        if (participantId !== userId) {
          const pc = getOrCreatePeerConnection(participantId);

          try {
            // Create and send offer
            const offer = await pc.createOffer();
            await pc.setLocalDescription(offer);

            onSignalingMessage({
              type: 'OFFER',
              meetingId,
              senderId: userId,
              fromUserId: userId,
              targetUserId: participantId,
              toUserId: participantId,
              data: {
                offer: offer.toJSON(),
              },
              offer: offer.toJSON(),
            });

            setParticipants((prev) => new Set([...prev, participantId]));
          } catch (error) {
            console.error(`Error creating offer for user ${participantId}:`, error);
          }
        }
      }

      // Notify others that we joined
      onSignalingMessage({
        type: 'USER_JOINED',
        meetingId,
        senderId: userId,
        fromUserId: userId,
      });
    },
    [userId, meetingId, localStream, getOrCreatePeerConnection, onSignalingMessage, isInitializedRef]
  );

  /**
   * Toggle microphone mute/unmute
   */
  const toggleMute = useCallback((): boolean => {
    if (!localStream) {
      return isMuted;
    }

    const audioTracks = localStream.getAudioTracks();
    const newMutedState = !isMuted;

    audioTracks.forEach((track) => {
      track.enabled = !newMutedState;
    });

    setIsMuted(newMutedState);
    return newMutedState;
  }, [localStream, isMuted]);

  /**
   * Toggle video on/off
   */
  const toggleVideo = useCallback((): boolean => {
    if (!localStream) {
      return !isVideoEnabled;
    }

    const videoTracks = localStream.getVideoTracks();
    const newVideoState = !isVideoEnabled;

    videoTracks.forEach((track) => {
      track.enabled = newVideoState;
    });

    setIsVideoEnabled(newVideoState);
    return newVideoState;
  }, [localStream, isVideoEnabled]);

  /**
   * Start screen sharing
   */
  const startScreenShare = useCallback(async (): Promise<void> => {
    try {
      if (screenStreamRef.current) {
        await stopScreenShare();
      }

      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          displaySurface: 'monitor',
          width: { ideal: 1920 },
          height: { ideal: 1080 },
        } as MediaTrackConstraints,
        audio: true,
      });

      screenStreamRef.current = stream;
      setIsScreenSharing(true);

      // Replace video track in all peer connections
      const videoTrack = stream.getVideoTracks()[0];
      peerConnectionsRef.current.forEach(({ pc }) => {
        const sender = pc.getSenders().find((s) => s.track?.kind === 'video');
        if (sender && videoTrack) {
          sender.replaceTrack(videoTrack);
        }
      });

      // Handle screen share stop (user clicks stop sharing in browser)
      videoTrack.onended = () => {
        stopScreenShare().catch((error) => {
          console.error('Error stopping screen share:', error);
        });
      };
    } catch (error) {
      const err = error instanceof Error ? error : new Error('Failed to start screen sharing');
      console.error('Error starting screen share:', err);
      onError?.(err);
      throw err;
    }
  }, [onError]);

  /**
   * Stop screen sharing
   */
  const stopScreenShare = useCallback(async (): Promise<void> => {
    if (screenStreamRef.current) {
      screenStreamRef.current.getTracks().forEach((track) => track.stop());
      screenStreamRef.current = null;
    }

    setIsScreenSharing(false);

    // Restore camera video track
    if (localStream && isVideoEnabled) {
      const videoTrack = localStream.getVideoTracks()[0];
      if (videoTrack) {
        peerConnectionsRef.current.forEach(({ pc }) => {
          const sender = pc.getSenders().find((s) => s.track?.kind === 'video');
          if (sender && videoTrack) {
            sender.replaceTrack(videoTrack);
          }
        });
      }
    }
  }, [localStream, isVideoEnabled]);

  /**
   * Leave meeting and cleanup
   */
  const leave = useCallback(() => {
    // Notify others that we're leaving
    onSignalingMessage({
      type: 'USER_LEFT',
      meetingId,
      senderId: userId,
      fromUserId: userId,
    });

    // Close all peer connections
    peerConnectionsRef.current.forEach(({ pc }) => {
      pc.close();
    });
    peerConnectionsRef.current.clear();

    // Stop local streams
    if (localStream) {
      localStream.getTracks().forEach((track) => track.stop());
    }

    if (screenStreamRef.current) {
      screenStreamRef.current.getTracks().forEach((track) => track.stop());
      screenStreamRef.current = null;
    }

    // Reset state
    setLocalStream(null);
    setRemoteStreams(new Map());
    setParticipants(new Set());
    setIsMuted(false);
    setIsVideoEnabled(false);
    setIsScreenSharing(false);
    isInitializedRef.current = false;
  }, [meetingId, userId, localStream, onSignalingMessage]);

  // Expose handleSignalingMessage via ref for external use
  const handleSignalingMessageRef = useRef(handleSignalingMessage);
  useEffect(() => {
    handleSignalingMessageRef.current = handleSignalingMessage;
    // Store reference for external handlers
    (window as any).__handleWebRTCSignaling = handleSignalingMessageRef.current;
    return () => {
      delete (window as any).__handleWebRTCSignaling;
    };
  }, [handleSignalingMessage]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      leave();
    };
  }, [leave]);

  return {
    localStream,
    remoteStreams,
    isMuted,
    isVideoEnabled,
    isScreenSharing,
    participants,
    initialize,
    toggleMute,
    toggleVideo,
    startScreenShare,
    stopScreenShare,
    leave,
    // Expose joinMeeting and handleSignalingMessage for external use
    joinMeeting: joinMeeting as any,
    handleSignalingMessage: handleSignalingMessageRef.current,
  };
};

// Export joinMeeting separately for type compatibility
export type { UseWebRTCReturn };

