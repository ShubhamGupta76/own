/**
 * Video Call Component
 * WebRTC-based video calling with Teams-like UI
 * Supports multiple participants (mesh model)
 */

import { useEffect, useState, useRef } from 'react';
import { 
  HiMicrophone, 
  HiVideoCamera,
  HiDesktopComputer,
  HiPhone
} from 'react-icons/hi';
import { webrtcManager } from '../services/webrtc';
import { wsManager } from '../services/websocket';
import { useAuth } from '../contexts/AuthContext';
import type { WebRTCSignalingMessage, MeetingParticipant } from '../types/api';

interface VideoCallProps {
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
  isScreenSharing?: boolean;
}

export const VideoCall: React.FC<VideoCallProps> = ({ meetingId, onLeave, participantIds = [] }) => {
  const { user } = useAuth();
  const userId = user?.id || 0;

  const [participants, setParticipants] = useState<Map<number, ParticipantVideo>>(new Map());
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [isConnecting, setIsConnecting] = useState(true);
  const [error, setError] = useState('');

  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRefs = useRef<Map<number, HTMLVideoElement>>(new Map());
  const registeredCallbacksRef = useRef<Array<{ userId: number; callback: (stream: MediaStream) => void }>>([]);
    setParticipants((prev) => {
      const newMap = new Map(prev);
      if (!newMap.has(participantId)) {
        newMap.set(participantId, {
          userId: participantId,
          displayName: `User ${participantId}`,
          stream,
          isMuted: false,
          isVideoEnabled: true,
          isLocal: false,
        });
      } else {
        const existing = newMap.get(participantId)!;
        newMap.set(participantId, { ...existing, stream });
      }
      return newMap;
    });

    // Set remote video element
    setTimeout(() => {
      const videoElement = remoteVideoRefs.current.get(participantId);
      if (videoElement && stream) {
        videoElement.srcObject = stream;
      }
    }, 100);
  };
  
  const handleRemoteStream = (participantId: number, stream: MediaStream) => {
    handleRemoteStreamRef.current?.(participantId, stream);
  };

  // Initialize WebRTC and WebSocket
  useEffect(() => {
    const initializeCall = async () => {
      try {
        setIsConnecting(true);
        setError('');

        // Initialize local stream
        const localStream = await webrtcManager.initializeLocalStream(true, true);
        
        // Set local video
        if (localVideoRef.current) {
          localVideoRef.current.srcObject = localStream;
        }

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

        // Setup remote stream callbacks for known participants
        registeredCallbacksRef.current = [];
        participantIds.forEach((participantId) => {
          if (participantId !== userId) {
            const callback = (stream: MediaStream) => {
              if (handleRemoteStreamRef.current) {
                handleRemoteStreamRef.current(participantId, stream);
              }
            };
            webrtcManager.onRemoteStream(participantId, callback);
            registeredCallbacksRef.current.push({ userId: participantId, callback });
          }
        });

        // Setup WebSocket signaling
        const handleSignalingMessage = async (message: WebRTCSignalingMessage) => {
          await webrtcManager.handleSignalingMessage(message);
          
          // Handle new participants joining
          if (message.type === 'JOIN_MEETING' && message.fromUserId && message.fromUserId !== userId) {
            const callback = (stream: MediaStream) => {
              if (handleRemoteStreamRef.current) {
                handleRemoteStreamRef.current(message.fromUserId!, stream);
              }
            };
            webrtcManager.onRemoteStream(message.fromUserId, callback);
            registeredCallbacksRef.current.push({ userId: message.fromUserId, callback });
          }
          
          // Handle participants leaving
          if (message.type === 'LEAVE_MEETING' && message.fromUserId && message.fromUserId !== userId) {
            setParticipants((prev) => {
              const newMap = new Map(prev);
              newMap.delete(message.fromUserId!);
              return newMap;
            });
            
            // Remove callback
            const index = registeredCallbacksRef.current.findIndex(cb => cb.userId === message.fromUserId);
            if (index > -1) {
              const { userId: participantId, callback } = registeredCallbacksRef.current[index];
              webrtcManager.offRemoteStream(participantId, callback);
              registeredCallbacksRef.current.splice(index, 1);
            }
          }
        };

        wsManager.connectToMeeting(meetingId, userId, handleSignalingMessage);

        // Send signaling callback
        const sendSignaling = (message: WebRTCSignalingMessage) => {
          wsManager.sendMeetingSignaling(meetingId, message);
        };

        // Join meeting with WebRTC
        await webrtcManager.joinMeeting(meetingId, userId, participantIds, sendSignaling);

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
      webrtcManager.leaveMeeting();
      wsManager.disconnectFromMeeting(meetingId);
      
      // Remove remote stream callbacks
      registeredCallbacksRef.current.forEach(({ userId: participantId, callback }) => {
        webrtcManager.offRemoteStream(participantId, callback);
      });
      registeredCallbacksRef.current = [];
    };
  }, [meetingId, userId]);

  // Update video elements when participants change
  useEffect(() => {
    participants.forEach((participant) => {
      if (participant.isLocal) {
        if (localVideoRef.current && participant.stream) {
          localVideoRef.current.srcObject = participant.stream;
        }
      } else {
        const videoElement = remoteVideoRefs.current.get(participant.userId);
        if (videoElement && participant.stream) {
          videoElement.srcObject = participant.stream;
        }
      }
    });
  }, [participants]);

  // Handle mute toggle
  const handleToggleMute = () => {
    const newMutedState = webrtcManager.toggleMute();
    setIsMuted(newMutedState);
    
    setParticipants((prev) => {
      const newMap = new Map(prev);
      const local = newMap.get(userId);
      if (local) {
        newMap.set(userId, { ...local, isMuted: newMutedState });
      }
      return newMap;
    });
  };

  // Handle video toggle
  const handleToggleVideo = () => {
    const newVideoState = webrtcManager.toggleVideo();
    setIsVideoEnabled(newVideoState);
    
    setParticipants((prev) => {
      const newMap = new Map(prev);
      const local = newMap.get(userId);
      if (local) {
        newMap.set(userId, { ...local, isVideoEnabled: newVideoState });
      }
      return newMap;
    });
  };

  // Handle screen share
  const handleScreenShare = async () => {
    try {
      if (isScreenSharing) {
        await webrtcManager.stopScreenShare();
        setIsScreenSharing(false);
      } else {
        await webrtcManager.startScreenShare();
        setIsScreenSharing(true);
      }
    } catch (err: any) {
      console.error('Error toggling screen share:', err);
      alert(err.message || 'Failed to toggle screen sharing');
    }
  };

  // Handle leave meeting
  const handleLeave = () => {
    webrtcManager.leaveMeeting();
    wsManager.disconnectFromMeeting(meetingId);
    onLeave();
  };

  // Get participants array (excluding local)
  const remoteParticipants = Array.from(participants.values()).filter((p) => !p.isLocal);
  const localParticipant = participants.get(userId);

  // Calculate grid columns based on participant count
  const participantCount = remoteParticipants.length + (localParticipant ? 1 : 0);
  const gridCols = participantCount <= 1 ? 1 : participantCount <= 2 ? 2 : participantCount <= 4 ? 2 : 3;

  return (
    <div className="flex flex-col h-full bg-gray-900">
      {/* Error Message */}
      {error && (
        <div className="bg-red-600 text-white px-4 py-3 text-center">
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
      <div className={`flex-1 grid gap-2 p-2 ${
        gridCols === 1 ? 'grid-cols-1' : 
        gridCols === 2 ? 'grid-cols-2' : 
        'grid-cols-3'
      } overflow-auto`}>
        {/* Local Video */}
        {localParticipant && (
          <div className="relative bg-gray-800 rounded-lg overflow-hidden">
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
            className="relative bg-gray-800 rounded-lg overflow-hidden"
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
            {participant.isScreenSharing && (
              <div className="absolute top-2 left-2 bg-blue-600 px-2 py-1 rounded text-white text-xs">
                Sharing Screen
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Controls Bar */}
      <div className="bg-gray-800 px-6 py-4 flex items-center justify-center space-x-4">
        {/* Mute/Unmute Button */}
        <button
          onClick={handleToggleMute}
          className={`p-3 rounded-full transition-colors ${
            isMuted
              ? 'bg-red-600 hover:bg-red-700 text-white'
              : 'bg-gray-700 hover:bg-gray-600 text-white'
          }`}
          title={isMuted ? 'Unmute' : 'Mute'}
        >
          {isMuted ? (
            <HiMicrophoneOff className="w-6 h-6" />
          ) : (
            <HiMicrophone className="w-6 h-6" />
          )}
        </button>

        {/* Camera Toggle Button */}
        <button
          onClick={handleToggleVideo}
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
            <HiVideoCameraOff className="w-6 h-6" />
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

