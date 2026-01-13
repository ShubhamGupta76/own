/**
 * Channel View Component
 * Displays channel content with tabs: Chat, Files, Tasks, Meetings
 * Similar to Microsoft Teams channel view
 */

import React from 'react';
import { useParams, Navigate } from 'react-router-dom';
import { useTeam } from '../contexts/TeamContext';
import { ChatPage } from './ChatPage';
import { FilesPage } from './FilesPage';
import { TasksPage } from './TasksPage';
import { MeetingsPage } from './MeetingsPage';

export const ChannelView: React.FC = () => {
  const params = useParams<{ 
    teamId: string; 
    channelId: string; 
    tab?: string;
  }>();
  const { teamId, channelId, tab } = params;
  const { selectedChannel, selectedTeam } = useTeam();

  // If no channel/team in params, redirect to teams list
  if (!teamId || !channelId) {
    return <Navigate to="/app/teams" replace />;
  }

  // Get active tab from URL or default to 'chat'
  const activeTab = tab || 'chat';

  // Render content based on active tab
  const renderTabContent = () => {
    switch (activeTab) {
      case 'files':
        return <FilesPage channelId={Number(channelId)} teamId={Number(teamId)} />;
      case 'tasks':
        return <TasksPage channelId={Number(channelId)} teamId={Number(teamId)} />;
      case 'meetings':
        return <MeetingsPage channelId={Number(channelId)} teamId={Number(teamId)} />;
      case 'chat':
      default:
        return <ChatPage channelId={Number(channelId)} roomType="channel" />;
    }
  };

  // Show loading if channel/team doesn't match context (will sync via TeamsChannelsSidebar useEffect)
  if (!selectedChannel || selectedChannel.id !== Number(channelId) || 
      !selectedTeam || selectedTeam.id !== Number(teamId)) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="text-gray-500">Loading channel...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      {renderTabContent()}
    </div>
  );
};

