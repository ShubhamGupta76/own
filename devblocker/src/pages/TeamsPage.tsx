/**
 * Teams Page
 * Lists all teams and channels (like Microsoft Teams)
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { teamsApi, channelsApi } from '../api';
import { useAuth } from '../contexts/AuthContext';
import type { Team, Channel } from '../types/api';

export const TeamsPage: React.FC = () => {
  const navigate = useNavigate();
  const { teamId, channelId } = useParams<{ teamId?: string; channelId?: string }>();
  const { user } = useAuth();
  
  const [teams, setTeams] = useState<Team[]>([]);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Fetch teams on mount
  useEffect(() => {
    const fetchTeams = async () => {
      // Check if user has organizationId
      if (!user?.organizationId || user.organizationId === 0) {
        setError('Please create an organization first to access teams.');
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError('');
        const teamList = await teamsApi.getMyTeams();
        setTeams(teamList);

        // Auto-select first team or team from URL
        if (teamList.length > 0) {
          const team = teamId
            ? teamList.find((t) => t.id === Number(teamId))
            : teamList[0];
          
          if (team) {
            setSelectedTeam(team);
            // Fetch channels for selected team
            await fetchChannels(team.id);
          }
        }
      } catch (err: any) {
        const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message;
        if (errorMessage?.includes('Organization not found') || errorMessage?.includes('organization')) {
          setError('Please create an organization first to access teams.');
        } else {
          setError(errorMessage || 'Failed to load teams');
        }
        console.error('Error fetching teams:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTeams();
  }, [teamId, user?.organizationId]);

  // Fetch channels when team is selected
  const fetchChannels = async (teamId: number) => {
    try {
      const channelList = await channelsApi.getChannelsByTeam(teamId);
      setChannels(channelList);

      // Auto-select "General" channel or channel from URL
      if (channelList.length > 0) {
        const channel = channelId
          ? channelList.find((c) => c.id === Number(channelId))
          : channelList.find((c) => c.name.toLowerCase() === 'general') || channelList[0];
        
        if (channel) {
          setSelectedChannel(channel);
          // Navigate to channel chat
          navigate(`/app/teams/${teamId}/channels/${channel.id}`, { replace: true });
        }
      }
    } catch (err: any) {
      console.error('Error fetching channels:', err);
    }
  };

  const handleTeamSelect = async (team: Team) => {
    setSelectedTeam(team);
    await fetchChannels(team.id);
  };

  const handleChannelSelect = (channel: Channel) => {
    setSelectedChannel(channel);
    if (selectedTeam) {
      navigate(`/app/teams/${selectedTeam.id}/channels/${channel.id}`);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    const isOrgError = error.includes('organization') || error.includes('Organization');
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="max-w-md text-center">
          <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-6 py-4 rounded-lg mb-4">
            <h3 className="text-lg font-semibold mb-2">Organization Required</h3>
            <p className="text-sm mb-4">
              {error || 'You need to create an organization before you can access teams and channels.'}
            </p>
            {isOrgError && (
              <p className="text-xs text-yellow-700">
                After creating an organization, you'll be able to create teams and start collaborating.
              </p>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (teams.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="text-6xl mb-4">👥</div>
        <h2 className="text-2xl font-semibold text-gray-900 mb-2">No Teams Yet</h2>
        <p className="text-gray-600 mb-6">You're not a member of any teams yet.</p>
        <button
          onClick={() => navigate('/app/teams/new')}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Create Team
        </button>
      </div>
    );
  }

  return (
    <div className="flex h-full">
      {/* Teams Sidebar */}
      <div className="w-64 border-r border-gray-200 bg-white overflow-y-auto">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Teams</h2>
        </div>
        <ul className="divide-y divide-gray-200">
          {teams.map((team) => (
            <li key={team.id}>
              <button
                onClick={() => handleTeamSelect(team)}
                className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors ${
                  selectedTeam?.id === team.id ? 'bg-blue-50 border-l-4 border-blue-600' : ''
                }`}
              >
                <div className="flex items-center">
                  <div className="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center text-white font-medium mr-3">
                    {team.name.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{team.name}</p>
                    {team.description && (
                      <p className="text-xs text-gray-500 truncate">{team.description}</p>
                    )}
                  </div>
                </div>
              </button>
            </li>
          ))}
        </ul>
      </div>

      {/* Channels Sidebar */}
      {selectedTeam && (
        <div className="w-64 border-r border-gray-200 bg-white overflow-y-auto">
          <div className="p-4 border-b border-gray-200 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">Channels</h2>
            <button
              onClick={() => navigate(`/app/teams/${selectedTeam.id}/channels/new`)}
              className="text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
              + New
            </button>
          </div>
          <ul className="divide-y divide-gray-200">
            {channels.map((channel) => (
              <li key={channel.id}>
                <button
                  onClick={() => handleChannelSelect(channel)}
                  className={`w-full text-left px-4 py-2 hover:bg-gray-50 transition-colors ${
                    selectedChannel?.id === channel.id ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
                  }`}
                >
                  <div className="flex items-center">
                    <span className="mr-2">#</span>
                    <span className="text-sm font-medium">{channel.name}</span>
                  </div>
                  {channel.description && (
                    <p className="text-xs text-gray-500 mt-1 ml-5 truncate">{channel.description}</p>
                  )}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        {selectedChannel ? (
          <div className="text-center">
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              {selectedTeam?.name} → {selectedChannel.name}
            </h3>
            <p className="text-gray-600 mb-4">Select a channel to start chatting</p>
            <button
              onClick={() => navigate(`/app/teams/${selectedTeam?.id}/channels/${selectedChannel.id}/chat`)}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Open Chat
            </button>
          </div>
        ) : (
          <div className="text-center">
            <div className="text-6xl mb-4">💬</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Select a Channel</h3>
            <p className="text-gray-600">Choose a channel from the list to start chatting</p>
          </div>
        )}
      </div>
    </div>
  );
};
