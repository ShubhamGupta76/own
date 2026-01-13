/**
 * Teams Landing Page
 * Shown when user navigates to /app/teams without selecting a channel
 * Displays a welcome message and instructions
 */

import React from 'react';
import { useTeam } from '../contexts/TeamContext';
import { HiOutlineUserGroup, HiArrowRight } from 'react-icons/hi';

export const TeamsLandingPage: React.FC = () => {
  const { teams, isLoading, error } = useTeam();

  if (error && error.includes('organization')) {
    return (
      <div className="flex items-center justify-center h-full p-6">
        <div className="max-w-md text-center">
          <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-6 py-4 rounded-lg">
            <h3 className="text-lg font-semibold mb-2">Organization Required</h3>
            <p className="text-sm">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-500">Loading teams...</div>
      </div>
    );
  }

  if (teams.length === 0) {
    return (
      <div className="flex items-center justify-center h-full p-6">
        <div className="max-w-2xl text-center">
          <div className="mb-6">
            <HiOutlineUserGroup className="w-20 h-20 text-gray-400 mx-auto mb-4" />
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Welcome to Teams</h1>
            <p className="text-gray-600 mb-6">
              Get started by creating a team or joining an existing one.
            </p>
          </div>
          <div className="bg-white rounded-lg border border-gray-200 p-6 text-left">
            <h2 className="font-semibold text-gray-900 mb-3">Getting Started</h2>
            <ul className="space-y-2 text-sm text-gray-600">
              <li className="flex items-start">
                <HiArrowRight className="w-5 h-5 text-blue-600 mr-2 mt-0.5 flex-shrink-0" />
                <span>Select a team from the sidebar to view channels</span>
              </li>
              <li className="flex items-start">
                <HiArrowRight className="w-5 h-5 text-blue-600 mr-2 mt-0.5 flex-shrink-0" />
                <span>Click on a channel to start chatting and collaborating</span>
              </li>
              <li className="flex items-start">
                <HiArrowRight className="w-5 h-5 text-blue-600 mr-2 mt-0.5 flex-shrink-0" />
                <span>Use the + button in the sidebar to create a new team</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center h-full p-6">
      <div className="max-w-2xl text-center">
        <HiOutlineUserGroup className="w-20 h-20 text-gray-400 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Select a Team</h1>
        <p className="text-gray-600 mb-6">
          Choose a team from the sidebar to get started.
        </p>
        <div className="bg-white rounded-lg border border-gray-200 p-6 text-left">
          <p className="text-sm text-gray-600">
            You have access to <strong>{teams.length}</strong> team{teams.length !== 1 ? 's' : ''}. 
            Click on a team in the sidebar to view its channels and start collaborating.
          </p>
        </div>
      </div>
    </div>
  );
};

