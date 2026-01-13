/**
 * Admin Teams Management Page
 * Route: /app/admin/teams
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';

export const AdminTeamsPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Teams Management</h1>
          <p className="text-gray-600">Manage organization teams and structure</p>
        </div>
        <button 
          onClick={() => navigate('/app/teams')}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          View Teams
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <p className="text-gray-600">Teams management functionality coming soon...</p>
      </div>
    </div>
  );
};

