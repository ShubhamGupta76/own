/**
 * Admin Dashboard Page
 * Main dashboard for admin users
 * Route: /app/admin/dashboard
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { HiUsers, HiUserGroup, HiShieldCheck, HiChartBar } from 'react-icons/hi';

export const AdminDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const stats = [
    { label: 'Total Users', value: '0', icon: HiUsers, color: 'blue', path: '/app/admin/users' },
    { label: 'Teams', value: '0', icon: HiUserGroup, color: 'green', path: '/app/admin/teams' },
    { label: 'Active Policies', value: '0', icon: HiShieldCheck, color: 'purple', path: '/app/admin/policies' },
    { label: 'System Health', value: '100%', icon: HiChartBar, color: 'yellow', path: '/app/admin/settings' },
  ];

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Admin Dashboard</h1>
        <p className="text-gray-600">Welcome back, {user?.email}</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <div
            key={stat.label}
            onClick={() => navigate(stat.path)}
            className="bg-white rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow border border-gray-200"
          >
            <div className="flex items-center justify-between mb-4">
              <div className={`w-12 h-12 bg-${stat.color}-100 rounded-lg flex items-center justify-center`}>
                <stat.icon className={`w-6 h-6 text-${stat.color}-600`} />
              </div>
            </div>
            <div>
              <p className="text-3xl font-bold text-gray-900 mb-1">{stat.value}</p>
              <p className="text-sm text-gray-600">{stat.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button
            onClick={() => navigate('/app/admin/users')}
            className="p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors text-left"
          >
            <HiUsers className="w-6 h-6 text-blue-600 mb-2" />
            <h3 className="font-semibold text-gray-900 mb-1">Manage Users</h3>
            <p className="text-sm text-gray-600">Add, edit, or remove users</p>
          </button>

          <button
            onClick={() => navigate('/app/admin/teams')}
            className="p-4 border-2 border-gray-200 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors text-left"
          >
            <HiUserGroup className="w-6 h-6 text-green-600 mb-2" />
            <h3 className="font-semibold text-gray-900 mb-1">Manage Teams</h3>
            <p className="text-sm text-gray-600">Create and manage teams</p>
          </button>

          <button
            onClick={() => navigate('/app/admin/policies')}
            className="p-4 border-2 border-gray-200 rounded-lg hover:border-purple-500 hover:bg-purple-50 transition-colors text-left"
          >
            <HiShieldCheck className="w-6 h-6 text-purple-600 mb-2" />
            <h3 className="font-semibold text-gray-900 mb-1">Security Policies</h3>
            <p className="text-sm text-gray-600">Configure security settings</p>
          </button>
        </div>
      </div>
    </div>
  );
};

