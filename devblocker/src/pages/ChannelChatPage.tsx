/**
 * Channel Chat Page
 * Chat view for a specific channel
 */

import React from 'react';
import { useParams } from 'react-router-dom';
import { ChatPage } from './ChatPage';

export const ChannelChatPage: React.FC = () => {
  const { channelId } = useParams<{ teamId: string; channelId: string }>();

  if (!channelId) {
    return <div className="p-6 text-center text-gray-500">Channel ID is required</div>;
  }

  return <ChatPage channelId={Number(channelId)} roomType="channel" />;
};

