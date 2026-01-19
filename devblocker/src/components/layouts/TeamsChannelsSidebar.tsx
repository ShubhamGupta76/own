import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { HiChevronRight, HiChevronDown, HiPlus, HiHashtag, HiLockClosed, HiUserAdd } from 'react-icons/hi';
import { useTeam } from '../../contexts/TeamContext';
import { useAuth } from '../../contexts/AuthContext';
import { teamsApi, userApi } from '../../api';
import type { Team, Channel, User } from '../../types/api';

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
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const [newTeamDescription, setNewTeamDescription] = useState('');
  const [newChannelName, setNewChannelName] = useState('');
  const [newChannelDescription, setNewChannelDescription] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  
  // Add member modal state
  const [organizationMembers, setOrganizationMembers] = useState<User[]>([]);
  const [isLoadingMembers, setIsLoadingMembers] = useState(false);
  const [addMemberError, setAddMemberError] = useState('');
  const [isAddingMember, setIsAddingMember] = useState(false);

  const canCreateTeam = role === 'ADMIN' || role === 'MANAGER';
  const canCreateChannel = (role === 'ADMIN' || role === 'MANAGER' || role === 'EMPLOYEE');
  const canAddMembers = role === 'ADMIN' || role === 'MANAGER';
  
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

  // Load organization members for the add member modal
  const loadOrganizationMembers = async () => {
    try {
      setIsLoadingMembers(true);
      setAddMemberError('');
      const members = await userApi.getOrganizationMembers();
      setOrganizationMembers(members);
    } catch (err: any) {
      const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message;
      setAddMemberError(errorMessage || 'Failed to load organization members');
      console.error('Error loading organization members:', err);
    } finally {
      setIsLoadingMembers(false);
    }
  };

  // Handle adding a member to the team
  const handleAddMember = async (userId: number) => {
    if (!selectedTeam) {
      setAddMemberError('No team selected');
      return;
    }
    
    try {
      setIsAddingMember(true);
      setAddMemberError('');
      
      await teamsApi.addMember(selectedTeam.id, userId, 'MEMBER');
      
      // Success - remove the added user from the list
      setOrganizationMembers((prev) => prev.filter((m) => m.id !== userId));
      
      // Refresh teams to reflect the new member (this will trigger a re-fetch)
      // The team context should handle refreshing
      
    } catch (err: any) {
      const status = err.response?.status;
      const errorMessage = err.response?.data?.error || err.response?.data?.message || err.message || 'Failed to add member to team';
      
      if (status === 403) {
        setAddMemberError('Access denied: You do not have permission to add members to this team.');
      } else if (status === 404) {
        setAddMemberError('Team not found or user not found.');
      } else if (errorMessage.includes('already a member')) {
        setAddMemberError('This user is already a member of the team.');
        // Remove from list since they're already a member
        setOrganizationMembers((prev) => prev.filter((m) => m.id !== userId));
      } else {
        setAddMemberError(errorMessage);
      }
      console.error('Error adding team member:', err);
    } finally {
      setIsAddingMember(false);
    }
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
                      
                      {isSelected && canAddMembers && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setShowAddMemberModal(true);
                            loadOrganizationMembers();
                          }}
                          className="w-full px-4 py-2 flex items-center space-x-2 text-sm text-blue-600 hover:text-blue-700 hover:bg-blue-50"
                        >
                          <HiUserAdd className="w-4 h-4" />
                          <span>Add Members</span>
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

      {/* Add Member Modal */}
      {showAddMemberModal && selectedTeam && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[80vh] flex flex-col">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900">
                Add Member to {selectedTeam.name}
              </h3>
            </div>
            
            <div className="p-6 flex-1 overflow-y-auto">
              {addMemberError && (
                <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                  {addMemberError}
                </div>
              )}
              
              {isLoadingMembers ? (
                <div className="text-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                  <p className="mt-2 text-sm text-gray-600">Loading members...</p>
                </div>
              ) : organizationMembers.length === 0 ? (
                <p className="text-sm text-gray-600 text-center py-4">No organization members found.</p>
              ) : (
                <div className="space-y-2">
                  {organizationMembers.map((member) => (
                    <div
                      key={member.id}
                      className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                    >
                      <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium mr-3">
                          {member.firstName?.charAt(0) || member.email.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {member.firstName} {member.lastName}
                          </p>
                          <p className="text-xs text-gray-500">{member.email}</p>
                          <p className="text-xs text-gray-400">{member.role}</p>
                        </div>
                      </div>
                      <button
                        onClick={() => handleAddMember(member.id)}
                        disabled={isAddingMember}
                        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                      >
                        {isAddingMember ? 'Adding...' : 'Add'}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
            
            <div className="p-6 border-t border-gray-200 flex justify-end">
              <button
                onClick={() => {
                  setShowAddMemberModal(false);
                  setAddMemberError('');
                  setOrganizationMembers([]);
                }}
                className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </aside>
  );
};

