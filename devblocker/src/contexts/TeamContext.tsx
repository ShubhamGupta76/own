import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { teamsApi, channelsApi } from '../api';
import { useAuth } from './AuthContext';
import type { Team, Channel } from '../types/api';

interface TeamContextType {
  teams: Team[];
  channels: Record<number, Channel[]>; // teamId -> channels[]
  expandedTeams: Set<number>;
  selectedTeam: Team | null;
  selectedChannel: Channel | null;
  isLoading: boolean;
  error: string | null;
  toggleTeam: (teamId: number) => void;
  selectTeam: (team: Team | null) => void;
  selectChannel: (channel: Channel | null) => void;
  refreshTeams: () => Promise<void>;
  refreshChannels: (teamId: number) => Promise<void>;
  createTeam: (name: string, description?: string) => Promise<Team>;
  createChannel: (teamId: number, name: string, description?: string) => Promise<Channel>;
}

const TeamContext = createContext<TeamContextType | undefined>(undefined);

export const useTeam = () => {
  const context = useContext(TeamContext);
  if (!context) {
    throw new Error('useTeam must be used within a TeamProvider');
  }
  return context;
};

interface TeamProviderProps {
  children: ReactNode;
}

export const TeamProvider: React.FC<TeamProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const [teams, setTeams] = useState<Team[]>([]);
  const [channels, setChannels] = useState<Record<number, Channel[]>>({});
  const [expandedTeams, setExpandedTeams] = useState<Set<number>>(new Set());
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshTeams = async () => {
    if (!user?.organizationId || user.organizationId === 0) {
      setError('Please create an organization first to access teams.');
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const fetchedTeams = await teamsApi.getMyTeams();
      setTeams(fetchedTeams);
      
      if (fetchedTeams.length > 0 && expandedTeams.size === 0) {
        setExpandedTeams(new Set([fetchedTeams[0].id]));
        setSelectedTeam(fetchedTeams[0]);
        await refreshChannels(fetchedTeams[0].id);
      }
    } catch (err: any) {
      console.error('Error fetching teams:', err);
      setError(err.response?.data?.message || 'Failed to fetch teams');
    } finally {
      setIsLoading(false);
    }
  };

  const refreshChannels = async (teamId: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const fetchedChannels = await channelsApi.getChannelsByTeam(teamId);
      setChannels((prev) => ({
        ...prev,
        [teamId]: fetchedChannels,
      }));
      
      if (fetchedChannels.length > 0 && (!selectedChannel || selectedChannel.teamId !== teamId)) {
        const generalChannel = fetchedChannels.find((c) => c.name.toLowerCase() === 'general');
        setSelectedChannel(generalChannel || fetchedChannels[0]);
      }
    } catch (err: any) {
      console.error('Error fetching channels:', err);
      setError(err.response?.data?.message || 'Failed to fetch channels');
    } finally {
      setIsLoading(false);
    }
  };

  const toggleTeam = (teamId: number) => {
    setExpandedTeams((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(teamId)) {
        newSet.delete(teamId);
      } else {
        newSet.add(teamId);
        if (!channels[teamId]) {
          refreshChannels(teamId);
        }
      }
      return newSet;
    });
  };

  const selectTeam = async (team: Team | null) => {
    setSelectedTeam(team);
    setSelectedChannel(null);
    
    if (team) {
      if (!expandedTeams.has(team.id)) {
        setExpandedTeams((prev) => new Set([...prev, team.id]));
      }
      
      if (!channels[team.id]) {
        await refreshChannels(team.id);
      } else {
        const teamChannels = channels[team.id];
        if (teamChannels.length > 0) {
          const generalChannel = teamChannels.find((c) => c.name.toLowerCase() === 'general');
          setSelectedChannel(generalChannel || teamChannels[0]);
        }
      }
    }
  };

  const selectChannel = (channel: Channel | null) => {
    setSelectedChannel(channel);
    if (channel) {
      const parentTeam = teams.find((t) => t.id === channel.teamId);
      if (parentTeam && parentTeam.id !== selectedTeam?.id) {
        setSelectedTeam(parentTeam);
      }
    }
  };

  const createTeam = async (name: string, description?: string): Promise<Team> => {
    if (!user?.organizationId || user.organizationId === 0) {
      throw new Error('Please create an organization first');
    }

    if (user.role !== 'ADMIN' && user.role !== 'MANAGER') {
      throw new Error('Only ADMIN and MANAGER roles can create teams');
    }

    try {
      const newTeam = await teamsApi.createTeam({ name, description });
      await refreshTeams();
      return newTeam;
    } catch (err: any) {
      console.error('Error creating team:', err);
      
      let errorMessage = 'Failed to create team';
      if (err.response) {
        if (err.response.status === 403) {
          errorMessage = 'Access denied: Only ADMIN and MANAGER roles can create teams';
        } else if (err.response.status === 400) {
          errorMessage = err.response.data?.message || 'Invalid request. Please check your input.';
        } else if (err.response.data?.message) {
          errorMessage = err.response.data.message;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      throw new Error(errorMessage);
    }
  };

  const createChannel = async (
    teamId: number,
    name: string,
    description?: string
  ): Promise<Channel> => {
    try {
      const newChannel = await channelsApi.createChannel(teamId, {
        name,
        description,
      });
      await refreshChannels(teamId);
      return newChannel;
    } catch (err: any) {
      console.error('Error creating channel:', err);
      throw new Error(err.response?.data?.message || 'Failed to create channel');
    }
  };

  useEffect(() => {
    if (user?.organizationId && user.organizationId > 0) {
      refreshTeams();
    }
  }, [user?.organizationId]);

  const value: TeamContextType = {
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
    refreshTeams,
    refreshChannels,
    createTeam,
    createChannel,
  };

  return <TeamContext.Provider value={value}>{children}</TeamContext.Provider>;
};

