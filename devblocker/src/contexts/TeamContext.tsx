/**
 * Team Context
 * Manages teams, channels, and current selection state
 */

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

  // Fetch teams
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
      
      // Auto-expand first team if none selected
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

  // Fetch channels for a team
  const refreshChannels = async (teamId: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const fetchedChannels = await channelsApi.getChannelsByTeam(teamId);
      setChannels((prev) => ({
        ...prev,
        [teamId]: fetchedChannels,
      }));
      
      // Auto-select "General" channel or first channel if none selected
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

  // Toggle team expansion
  const toggleTeam = (teamId: number) => {
    setExpandedTeams((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(teamId)) {
        newSet.delete(teamId);
      } else {
        newSet.add(teamId);
        // Auto-fetch channels if not loaded
        if (!channels[teamId]) {
          refreshChannels(teamId);
        }
      }
      return newSet;
    });
  };

  // Select a team
  const selectTeam = async (team: Team | null) => {
    setSelectedTeam(team);
    setSelectedChannel(null); // Clear channel selection when team changes
    
    if (team) {
      // Expand team if not already expanded
      if (!expandedTeams.has(team.id)) {
        setExpandedTeams((prev) => new Set([...prev, team.id]));
      }
      
      // Fetch channels if not loaded
      if (!channels[team.id]) {
        await refreshChannels(team.id);
      } else {
        // Auto-select first channel or General
        const teamChannels = channels[team.id];
        if (teamChannels.length > 0) {
          const generalChannel = teamChannels.find((c) => c.name.toLowerCase() === 'general');
          setSelectedChannel(generalChannel || teamChannels[0]);
        }
      }
    }
  };

  // Select a channel
  const selectChannel = (channel: Channel | null) => {
    setSelectedChannel(channel);
    if (channel) {
      // Ensure parent team is selected
      const parentTeam = teams.find((t) => t.id === channel.teamId);
      if (parentTeam && parentTeam.id !== selectedTeam?.id) {
        setSelectedTeam(parentTeam);
      }
    }
  };

  // Create a new team
  const createTeam = async (name: string, description?: string): Promise<Team> => {
    if (!user?.organizationId || user.organizationId === 0) {
      throw new Error('Please create an organization first');
    }

    try {
      const newTeam = await teamsApi.createTeam({ name, description });
      await refreshTeams();
      return newTeam;
    } catch (err: any) {
      console.error('Error creating team:', err);
      throw new Error(err.response?.data?.message || 'Failed to create team');
    }
  };

  // Create a new channel
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

  // Initial fetch on mount
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

