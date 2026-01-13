/**
 * MeetingRoom Component
 * WebRTC-based video calling with Teams-like UI
 * Uses useWebRTC hook for peer-to-peer connections
 */

import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  HiMicrophone,
  HiVideoCamera,
  HiDesktopComputer,
  HiPhone,
} from 'react-icons/hi';
import { useWebRTC } from '../hooks/useWebRTC';
import { wsManager } from '../services/websocket';
import { useAuth } from '../contexts/AuthContext';
import type { WebRTCSignalingMessage } from '../types/api';

interface MeetingRoomProps {
  meetingId: number;
  onLeave: () => void;
  participantIds?: number[]; // Initial list of participants
}

interface ParticipantVideo {
  userId: number;
  displayName: string;
  stream: MediaStream | null;
  isMuted: boolean;
  isVideoEnabled: boolean;
  isLocal: boolean;
}

export const MeetingRoom: React.FC<MeetingRoomProps> = ({ meetingId, onLeave, participantIds = [] }) => {
  const { user } = useAuth();
  const userId = user?.id || 0;

  const [participants, setParticipants] = useState<Map<number, ParticipantVideo>>(new Map());
  const [isConnecting, setIsConnecting] = useState(true);
  const [error, setError] = useState('');

  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRefs = useRef<Map<number, HTMLVideoElement>>(new Map());

  // Handle signaling message via WebSocket
  const handleSignalingMessage = useRef<(message: WebRTCSignalingMessage) => void>();

  // Setup WebRTC hook
  const webrtcHook = useWebRTC({
    meetingId,
    userId,
    onSignalingMessage: (message) => {
      // Send via WebSocket
      wsManager.sendMeetingSignaling(meetingId, message);
    },
    onRemoteStream: (participantUserId, stream) => {
      // Update participant video
      setParticipants((prev) => {
        const newMap = new Map(prev);
        if (!newMap.has(participantUserId)) {
          newMap.set(participantUserId, {
            userId: participantUserId,
            displayName: `User ${participantUserId}`,
            stream,
            isMuted: false,
            isVideoEnabled: true,
            isLocal: false,
          });
        } else {
          const existing = newMap.get(participantUserId)!;
          newMap.set(participantUserId, { ...existing, stream });
        }
        return newMap;
      });

      // Set remote video element
      setTimeout(() => {
        const videoElement = remoteVideoRefs.current.get(participantUserId);
        if (videoElement && stream) {
          videoElement.srcObject = stream;
        }
      }, 100);
    },
    onUserJoined: (participantUserId) => {
      setParticipantUserIds((prev) => new Set([...prev, participantUserId]));
    },
    onUserLeft: (participantUserId) => {
      setParticipantUserIds((prev) => {
        const newSet = new Set(prev);
        newSet.delete(participantUserId);
        return newSet;
      });
      setParticipants((prev) => {
        const newMap = new Map(prev);
        newMap.delete(participantUserId);
        return newMap;
      });
    },
    onError: (err) => {
      console.error('WebRTC error:', err);
      setError(err.message);
    },
  });

  // Destructure hook values
  const {
    localStream,
    remoteStreams,
    isMuted,
    isVideoEnabled,
    isScreenSharing,
    participants: webrtcParticipants,
    initialize,
    toggleMute,
    toggleVideo,
    startScreenShare,
    stopScreenShare,
    leave: leaveWebRTC,
    joinMeeting,
    handleSignalingMessage: handleWebRTCSignaling,
  } = webrtcHook;

  // Initialize WebRTC and WebSocket
  useEffect(() => {
    const initializeCall = async () => {
      try {
        setIsConnecting(true);
        setError('');

        // Initialize local media stream
        await initialize(true, true);

        // Add local participant
        setParticipants((prev) => {
          const newMap = new Map(prev);
          newMap.set(userId, {
            userId,
            displayName: user?.displayName || user?.email || 'You',
            stream: localStream,
            isMuted: false,
            isVideoEnabled: true,
            isLocal: true,
          });
          return newMap;
        });

        // Setup WebSocket signaling handler
        const handleWebSocketSignaling = async (message: WebRTCSignalingMessage) => {
          // Forward to WebRTC hook's handleSignalingMessage
          // Use ref if available, otherwise try to get from hook
          try {
            if (handleWebRTCSignaling) {
              await handleWebRTCSignaling(message);
            } else {
              // Fallback: use global handler
              const handler = (window as any).__handleWebRTCSignaling;
              if (handler) {
                await handler(message);
              }
            }
          } catch (error) {
            console.error('Error handling signaling message:', error);
          }
        };

        // Connect to meeting WebSocket
        wsManager.connectToMeeting(meetingId, userId, handleWebSocketSignaling);
        handleSignalingMessage.current = handleWebSocketSignaling;

        // Join meeting with existing participants
        if (participantIds.length > 0) {
          await (joinMeeting as any)(participantIds);
        }

        setIsConnecting(false);
      } catch (err: any) {
        console.error('Error initializing call:', err);
        setError(err.message || 'Failed to start video call. Please check camera/microphone permissions.');
        setIsConnecting(false);
      }
    };

    if (userId && meetingId) {
      initializeCall();
    }

    return () => {
      // Cleanup on unmount
      leaveWebRTC();
      wsManager.disconnectFromMeeting(meetingId);
    };
  }, [meetingId, userId]);

  // Update local video element when stream changes
  useEffect(() => {
    if (localVideoRef.current && localStream) {
      localVideoRef.current.srcObject = localStream;
    }
  }, [localStream]);

  // Update remote video elements when streams change
  useEffect(() => {
    remoteStreams.forEach((stream, participantUserId) => {
      const videoElement = remoteVideoRefs.current.get(participantUserId);
      if (videoElement && stream) {
        videoElement.srcObject = stream;
      }

      // Update participant with stream
      setParticipants((prev) => {
        const newMap = new Map(prev);
        const existing = newMap.get(participantUserId);
        if (existing) {
          newMap.set(participantUserId, { ...existing, stream });
        }
        return newMap;
      });
    });
  }, [remoteStreams]);

  // Update participants when WebRTC participants change
  useEffect(() => {
    setParticipantUserIds(webrtcParticipants);
  }, [webrtcParticipants]);

  // Update local participant state when mute/video changes
  useEffect(() => {
    setParticipants((prev) => {
      const newMap = new Map(prev);
      const local = newMap.get(userId);
      if (local) {
        newMap.set(userId, {
          ...local,
          isMuted,
          isVideoEnabled,
        });
      }
      return newMap;
    });
  }, [isMuted, isVideoEnabled, userId]);

  // Handle screen share toggle
  const handleScreenShare = async () => {
    try {
      if (isScreenSharing) {
        await stopScreenShare();
      } else {
        await startScreenShare();
      }
    } catch (err: any) {
      console.error('Error toggling screen share:', err);
      alert(err.message || 'Failed to toggle screen sharing');
    }
  };

  // Handle leave meeting
  const handleLeave = () => {
    leaveWebRTC();
    wsManager.disconnectFromMeeting(meetingId);
    onLeave();
  };

  // Get participants array
  const participantsArray = Array.from(participants.values());
  const localParticipant = participants.get(userId);
  const remoteParticipants = participantsArray.filter((p) => !p.isLocal);

  // Calculate grid columns based on participant count
  const participantCount = participantsArray.length;
  const gridCols = participantCount <= 1 ? 1 : participantCount <= 2 ? 2 : participantCount <= 4 ? 2 : 3;

  return (
    <div className="flex flex-col h-full bg-gray-900 relative">
      {/* Error Message */}
      {error && (
        <div className="bg-red-600 text-white px-4 py-3 text-center z-50">
          <p>{error}</p>
        </div>
      )}

      {/* Connecting Overlay */}
      {isConnecting && (
        <div className="absolute inset-0 bg-gray-900 bg-opacity-90 flex items-center justify-center z-50">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white mx-auto mb-4"></div>
            <p className="text-white text-lg">Connecting to meeting...</p>
          </div>
        </div>
      )}

      {/* Video Grid */}
      <div
        className={`flex-1 grid gap-2 p-2 overflow-auto ${
          gridCols === 1
            ? 'grid-cols-1'
            : gridCols === 2
            ? 'grid-cols-2'
            : 'grid-cols-3'
        }`}
      >
        {/* Local Video */}
        {localParticipant && (
          <div className="relative bg-gray-800 rounded-lg overflow-hidden min-h-[200px]">
            <video
              ref={localVideoRef}
              autoPlay
              muted
              playsInline
              className="w-full h-full object-cover"
            />
            {!localParticipant.isVideoEnabled && (
              <div className="absolute inset-0 bg-gray-800 flex items-center justify-center">
                <div className="text-center">
                  <div className="w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-2">
                    <span className="text-white text-xl font-semibold">
                      {(localParticipant.displayName || 'You').charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <p className="text-white text-sm">{localParticipant.displayName || 'You'}</p>
                </div>
              </div>
            )}
            <div className="absolute bottom-2 left-2 bg-black bg-opacity-60 px-2 py-1 rounded text-white text-xs">
              {localParticipant.displayName || 'You'} {localParticipant.isMuted ? '(Muted)' : ''}
            </div>
            {isScreenSharing && (
              <div className="absolute top-2 left-2 bg-blue-600 px-2 py-1 rounded text-white text-xs">
                Sharing Screen
              </div>
            )}
          </div>
        )}

        {/* Remote Videos */}
        {remoteParticipants.map((participant) => (
          <div
            key={participant.userId}
            className="relative bg-gray-800 rounded-lg overflow-hidden min-h-[200px]"
          >
            <video
              ref={(el) => {
                if (el) {
                  remoteVideoRefs.current.set(participant.userId, el);
                } else {
                  remoteVideoRefs.current.delete(participant.userId);
                }
              }}
              autoPlay
              playsInline
              className="w-full h-full object-cover"
            />
            {!participant.isVideoEnabled && (
              <div className="absolute inset-0 bg-gray-800 flex items-center justify-center">
                <div className="text-center">
                  <div className="w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-2">
                    <span className="text-white text-xl font-semibold">
                      {participant.displayName.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <p className="text-white text-sm">{participant.displayName}</p>
                </div>
              </div>
            )}
            <div className="absolute bottom-2 left-2 bg-black bg-opacity-60 px-2 py-1 rounded text-white text-xs">
              {participant.displayName} {participant.isMuted ? '(Muted)' : ''}
            </div>
          </div>
        ))}

        {/* Empty state */}
        {participantCount === 0 && !isConnecting && (
          <div className="col-span-full flex items-center justify-center h-full text-white text-center">
            <div>
              <p className="text-xl mb-2">Waiting for participants...</p>
              <p className="text-gray-400 text-sm">Share the meeting link to invite others</p>
            </div>
          </div>
        )}
      </div>

      {/* Controls Bar */}
      <div className="bg-gray-800 px-6 py-4 flex items-center justify-center space-x-4 shrink-0">
        {/* Mute/Unmute Button */}
        <button
          onClick={toggleMute}
          className={`p-3 rounded-full transition-colors ${
            isMuted
              ? 'bg-red-600 hover:bg-red-700 text-white'
              : 'bg-gray-700 hover:bg-gray-600 text-white'
          }`}
          title={isMuted ? 'Unmute' : 'Mute'}
        >
          {isMuted ? (
            <div className="relative inline-block">
              <HiMicrophone className="w-6 h-6 opacity-50" />
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-5 h-0.5 bg-red-500 transform rotate-45"></div>
              </div>
            </div>
          ) : (
            <HiMicrophone className="w-6 h-6" />
          )}
        </button>

        {/* Camera Toggle Button */}
        <button
          onClick={toggleVideo}
          className={`p-3 rounded-full transition-colors ${
            isVideoEnabled
              ? 'bg-gray-700 hover:bg-gray-600 text-white'
              : 'bg-red-600 hover:bg-red-700 text-white'
          }`}
          title={isVideoEnabled ? 'Turn off camera' : 'Turn on camera'}
        >
          {isVideoEnabled ? (
            <HiVideoCamera className="w-6 h-6" />
          ) : (
            <div className="relative inline-block">
              <HiVideoCamera className="w-6 h-6 opacity-50" />
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="w-5 h-0.5 bg-red-500 transform rotate-45"></div>
              </div>
            </div>
          )}
        </button>

        {/* Screen Share Button */}
        <button
          onClick={handleScreenShare}
          className={`p-3 rounded-full transition-colors ${
            isScreenSharing
              ? 'bg-blue-600 hover:bg-blue-700 text-white'
              : 'bg-gray-700 hover:bg-gray-600 text-white'
          }`}
          title={isScreenSharing ? 'Stop sharing' : 'Share screen'}
        >
          <HiDesktopComputer className="w-6 h-6" />
        </button>

        {/* Leave Button */}
        <button
          onClick={handleLeave}
          className="p-3 rounded-full bg-red-600 hover:bg-red-700 text-white transition-colors"
          title="Leave meeting"
        >
          <HiPhone className="w-6 h-6 transform rotate-135" />
        </button>

        {/* Participant Count */}
        <div className="text-white text-sm ml-4">
          {participantCount} participant{participantCount !== 1 ? 's' : ''}
        </div>
      </div>
    </div>
  );
};

