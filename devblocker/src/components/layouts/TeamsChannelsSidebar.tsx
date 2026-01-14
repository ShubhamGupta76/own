import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { HiChevronRight, HiChevronDown, HiPlus, HiHashtag, HiLockClosed } from 'react-icons/hi';
import { useTeam } from '../../contexts/TeamContext';
import { useAuth } from '../../contexts/AuthContext';
import type { Team, Channel } from '../../types/api';

export const TeamsChannelsSidebar: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams<{ teamId?: string; channelId?: string }>();
  const { 
    teams, 
    channels, 
    expandedTeams, 
    selectedTeam, 
    selectedChannel,
    isLoading,
    error,
    toggleTeam, 
    selectTeam, 
    selectChannel,
    createTeam,
    createChannel,
    refreshChannels,
  } = useTeam();
  const { role } = useAuth();

  useEffect(() => {
    if (params.teamId && params.channelId) {
      const teamId = Number(params.teamId);
      const channelId = Number(params.channelId);
      
      // Find and select team
      const team = teams.find((t) => t.id === teamId);
      if (team) {
        if (team.id !== selectedTeam?.id) {
          selectTeam(team);
        }
        if (!expandedTeams.has(teamId)) {
          toggleTeam(teamId);
        }
        
        const teamChannels = channels[teamId];
        if (teamChannels && teamChannels.length > 0) {
          const channel = teamChannels.find((c) => c.id === channelId);
          if (channel && channel.id !== selectedChannel?.id) {
            selectChannel(channel);
          }
        } else if (!channels[teamId] && !isLoading) {
          refreshChannels(teamId);
        }
      }
    }
  }, [params.teamId, params.channelId, teams.length, channels]);
  
  const [showCreateTeamModal, setShowCreateTeamModal] = useState(false);
  const [showCreateChannelModal, setShowCreateChannelModal] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const [newTeamDescription, setNewTeamDescription] = useState('');
  const [newChannelName, setNewChannelName] = useState('');
  const [newChannelDescription, setNewChannelDescription] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  const canCreateTeam = role === 'ADMIN' || role === 'MANAGER';
  const canCreateChannel = (role === 'ADMIN' || role === 'MANAGER' || role === 'EMPLOYEE');
  
  useEffect(() => {
    if (role) {
      console.debug('User role for team creation check:', role, 'canCreateTeam:', canCreateTeam);
    }
  }, [role, canCreateTeam]);

  const handleTeamClick = (team: Team) => {
    toggleTeam(team.id);
    selectTeam(team);
  };

  const handleChannelClick = (channel: Channel) => {
    selectChannel(channel);
    navigate(`/app/teams/${channel.teamId}/channels/${channel.id}`);
  };

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTeamName.trim()) return;

    if (!canCreateTeam) {
      alert('Only ADMIN and MANAGER roles can create teams');
      return;
    }

    setIsCreating(true);
    try {
      const team = await createTeam(newTeamName, newTeamDescription || undefined);
      setShowCreateTeamModal(false);
      setNewTeamName('');
      setNewTeamDescription('');
      navigate(`/app/teams/${team.id}`);
    } catch (err: any) {
      const errorMessage = err.message || 'Failed to create team';
      console.error('Error creating team:', err);
      alert(errorMessage);
    } finally {
      setIsCreating(false);
    }
  };

  const handleCreateChannel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChannelName.trim() || !selectedTeam) return;

    setIsCreating(true);
    try {
      const channel = await createChannel(
        selectedTeam.id,
        newChannelName,
        newChannelDescription || undefined
      );
      setShowCreateChannelModal(false);
      setNewChannelName('');
      setNewChannelDescription('');
      navigate(`/app/teams/${channel.teamId}/channels/${channel.id}`);
    } catch (err: any) {
      alert(err.message || 'Failed to create channel');
    } finally {
      setIsCreating(false);
    }
  };

  const sortChannels = (channelsList: Channel[]): Channel[] => {
    return [...channelsList].sort((a, b) => {
      if (a.name.toLowerCase() === 'general') return -1;
      if (b.name.toLowerCase() === 'general') return 1;
      return a.name.localeCompare(b.name);
    });
  };

  if (error && !error.includes('organization')) {
    return (
      <aside className="w-64 bg-gray-50 border-r border-gray-200 flex flex-col">
        <div className="p-4 text-sm text-red-600">{error}</div>
      </aside>
    );
  }

  return (
    <aside className="w-64 bg-gray-50 border-r border-gray-200 flex flex-col">
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">
            Teams & Channels
          </h2>
          {canCreateTeam && (
            <button
              onClick={() => setShowCreateTeamModal(true)}
              className="p-1 text-gray-500 hover:text-gray-700 hover:bg-gray-200 rounded"
              title="Create team"
            >
              <HiPlus className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {isLoading && teams.length === 0 ? (
          <div className="p-4 text-sm text-gray-500 text-center">Loading teams...</div>
        ) : teams.length === 0 ? (
          <div className="p-4 text-sm text-gray-500 text-center">
            {error?.includes('organization') 
              ? error 
              : 'No teams yet. Create one to get started!'}
          </div>
        ) : (
          <div className="py-2">
            {teams.map((team) => {
              const isExpanded = expandedTeams.has(team.id);
              const isSelected = selectedTeam?.id === team.id;
              const teamChannels = channels[team.id] || [];
              const sortedChannels = sortChannels(teamChannels);

              return (
                <div key={team.id} className="mb-1">
                  <button
                    onClick={() => handleTeamClick(team)}
                    className={`
                      w-full px-4 py-2 flex items-center justify-between text-sm font-medium
                      transition-colors
                      ${isSelected
                        ? 'bg-blue-50 text-blue-700'
                        : 'text-gray-700 hover:bg-gray-100'
                      }
                    `}
                  >
                    <div className="flex items-center space-x-2 flex-1 min-w-0">
                      {isExpanded ? (
                        <HiChevronDown className="w-4 h-4 flex-shrink-0" />
                      ) : (
                        <HiChevronRight className="w-4 h-4 flex-shrink-0" />
                      )}
                      <span className="truncate">{team.name}</span>
                    </div>
                  </button>

                  {isExpanded && (
                    <div className="ml-4 mt-1">
                      {teamChannels.length === 0 ? (
                        <div className="px-4 py-2 text-xs text-gray-500">
                          No channels
                        </div>
                      ) : (
                        sortedChannels.map((channel) => {
                          const isChannelSelected = selectedChannel?.id === channel.id;
                          const isPrivate = channel.type === 'PRIVATE';

                          return (
                            <button
                              key={channel.id}
                              onClick={() => handleChannelClick(channel)}
                              className={`
                                w-full px-4 py-2 flex items-center space-x-2 text-sm
                                transition-colors
                                ${isChannelSelected
                                  ? 'bg-blue-50 text-blue-700 font-medium'
                                  : 'text-gray-600 hover:bg-gray-100'
                                }
                              `}
                            >
                              {isPrivate ? (
                                <HiLockClosed className="w-4 h-4 flex-shrink-0" />
                              ) : (
                                <HiHashtag className="w-4 h-4 flex-shrink-0" />
                              )}
                              <span className="truncate">{channel.name}</span>
                            </button>
                          );
                        })
                      )}
                      
                      {isSelected && canCreateChannel && (
                        <button
                          onClick={() => setShowCreateChannelModal(true)}
                          className="w-full px-4 py-2 flex items-center space-x-2 text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100"
                        >
                          <HiPlus className="w-4 h-4" />
                          <span>Create channel</span>
                        </button>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {showCreateTeamModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96 max-w-[90vw]">
            <h3 className="text-lg font-semibold mb-4">Create Team</h3>
            <form onSubmit={handleCreateTeam}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Team Name *
                </label>
                <input
                  type="text"
                  value={newTeamName}
                  onChange={(e) => setNewTeamName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter team name"
                  required
                  autoFocus
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description (optional)
                </label>
                <textarea
                  value={newTeamDescription}
                  onChange={(e) => setNewTeamDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter team description"
                  rows={3}
                />
              </div>
              <div className="flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateTeamModal(false);
                    setNewTeamName('');
                    setNewTeamDescription('');
                  }}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isCreating || !newTeamName.trim()}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isCreating ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showCreateChannelModal && selectedTeam && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96 max-w-[90vw]">
            <h3 className="text-lg font-semibold mb-4">Create Channel in {selectedTeam.name}</h3>
            <form onSubmit={handleCreateChannel}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Channel Name *
                </label>
                <input
                  type="text"
                  value={newChannelName}
                  onChange={(e) => setNewChannelName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter channel name"
                  required
                  autoFocus
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description (optional)
                </label>
                <textarea
                  value={newChannelDescription}
                  onChange={(e) => setNewChannelDescription(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter channel description"
                  rows={3}
                />
              </div>
              <div className="flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateChannelModal(false);
                    setNewChannelName('');
                    setNewChannelDescription('');
                  }}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isCreating || !newChannelName.trim()}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isCreating ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </aside>
  );
};

