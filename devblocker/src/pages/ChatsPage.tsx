/**
 * Chats Page Component
 * Main chat interface with user list sidebar and direct messaging
 * Includes calling, video calling, and screen sharing features
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChatSidebar } from '../components/ChatSidebar';
import { ChatPage } from './ChatPage';
import { useAuth } from '../contexts/AuthContext';
import { meetingsApi } from '../api';
import type { User } from '../types/api';
import { HiPhone, HiVideoCamera, HiDesktopComputer } from 'react-icons/hi';

export const ChatsPage: React.FC = () => {
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isCreatingCall, setIsCreatingCall] = useState(false);

  const handleSelectUser = (user: User) => {
    setSelectedUser(user);
  };

  const createInstantMeeting = async (title: string, participantIds: number[]) => {
    try {
      setIsCreatingCall(true);
      const meeting = await meetingsApi.createInstantCall({
        title,
        description: `Direct call with ${selectedUser?.displayName || selectedUser?.email}`,
        participantIds,
      });
      return meeting;
    } catch (err: any) {
      console.error('Error creating meeting:', err);
      throw err;
    } finally {
      setIsCreatingCall(false);
    }
  };

  const handleVoiceCall = async () => {
    if (!selectedUser || !currentUser) return;
    
    try {
      const meeting = await createInstantMeeting(
        `Voice Call with ${selectedUser.displayName || selectedUser.email}`,
        [selectedUser.id]
      );
      // Navigate to meeting room (audio only will be handled in MeetingRoom)
      navigate(`/app/meetings/${meeting.id}/room?audioOnly=true`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to start voice call');
    }
  };

  const handleVideoCall = async () => {
    if (!selectedUser || !currentUser) return;
    
    try {
      const meeting = await createInstantMeeting(
        `Video Call with ${selectedUser.displayName || selectedUser.email}`,
        [selectedUser.id]
      );
      navigate(`/app/meetings/${meeting.id}/room`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to start video call');
    }
  };

  const handleScreenShare = async () => {
    if (!selectedUser || !currentUser) return;
    
    try {
      const meeting = await createInstantMeeting(
        `Screen Share with ${selectedUser.displayName || selectedUser.email}`,
        [selectedUser.id]
      );
      navigate(`/app/meetings/${meeting.id}/room?screenShare=true`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to start screen share');
    }
  };

  return (
    <div className="flex h-full bg-gray-50">
      {/* Sidebar with User List */}
      <ChatSidebar 
        onSelectUser={handleSelectUser}
        selectedUserId={selectedUser?.id}
      />

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        {selectedUser ? (
          <>
            {/* Chat Header with Call Buttons */}
            <div className="bg-white border-b border-gray-200 px-6 py-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">
                    {selectedUser.firstName?.charAt(0) || selectedUser.email.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">
                      {selectedUser.displayName || `${selectedUser.firstName} ${selectedUser.lastName}` || selectedUser.email}
                    </h2>
                    <p className="text-sm text-gray-500">
                      {selectedUser.role} • {selectedUser.email}
                    </p>
                  </div>
                </div>
                
                {/* Call Action Buttons */}
                <div className="flex items-center space-x-2">
                  <button
                    onClick={handleVoiceCall}
                    disabled={isCreatingCall}
                    className="p-2 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    title="Voice Call"
                  >
                    <HiPhone className="w-5 h-5 text-gray-600" />
                  </button>
                  <button
                    onClick={handleVideoCall}
                    disabled={isCreatingCall}
                    className="p-2 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    title="Video Call"
                  >
                    <HiVideoCamera className="w-5 h-5 text-gray-600" />
                  </button>
                  <button
                    onClick={handleScreenShare}
                    disabled={isCreatingCall}
                    className="p-2 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    title="Screen Share"
                  >
                    <HiDesktopComputer className="w-5 h-5 text-gray-600" />
                  </button>
                </div>
              </div>
            </div>

            {/* Chat Messages */}
            <div className="flex-1 overflow-hidden">
              <ChatPage 
                userId={selectedUser.id}
                roomType="direct"
              />
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-white">
            <div className="text-center">
              <div className="text-6xl mb-4">💬</div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Select a user to start chatting</h3>
              <p className="text-gray-500">
                Choose someone from the sidebar to begin a conversation
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

