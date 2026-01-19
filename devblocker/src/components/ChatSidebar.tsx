/**
 * Chat Sidebar Component
 * Shows list of users (employees, admins, managers) for direct messaging
 */

import React, { useState, useEffect } from 'react';
import { userApi } from '../api';
import { useAuth } from '../contexts/AuthContext';
import type { User } from '../types/api';
import { HiUser, HiUserGroup, HiShieldCheck, HiChat } from 'react-icons/hi';

interface ChatSidebarProps {
  onSelectUser: (user: User) => void;
  selectedUserId?: number;
}

export const ChatSidebar: React.FC<ChatSidebarProps> = ({ onSelectUser, selectedUserId }) => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterRole, setFilterRole] = useState<'ALL' | 'EMPLOYEE' | 'ADMIN' | 'MANAGER'>('ALL');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setIsLoading(true);
        const organizationMembers = await userApi.getOrganizationMembers();
        // Filter out current user
        const otherUsers = organizationMembers.filter(u => u.id !== currentUser?.id);
        setUsers(otherUsers);
      } catch (err: any) {
        console.error('Error fetching users:', err);
      } finally {
        setIsLoading(false);
      }
    };

    if (currentUser) {
      fetchUsers();
    }
  }, [currentUser]);

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return <HiShieldCheck className="w-4 h-4 text-purple-600" />;
      case 'MANAGER':
        return <HiUserGroup className="w-4 h-4 text-blue-600" />;
      default:
        return <HiUser className="w-4 h-4 text-gray-600" />;
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-purple-100 text-purple-700';
      case 'MANAGER':
        return 'bg-blue-100 text-blue-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const filteredUsers = users.filter(user => {
    const matchesSearch = 
      user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.displayName?.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesRole = filterRole === 'ALL' || user.role === filterRole;
    
    return matchesSearch && matchesRole;
  });

  // Group users by role
  const groupedUsers = {
    ADMIN: filteredUsers.filter(u => u.role === 'ADMIN'),
    MANAGER: filteredUsers.filter(u => u.role === 'MANAGER'),
    EMPLOYEE: filteredUsers.filter(u => u.role === 'EMPLOYEE'),
  };

  if (isLoading) {
    return (
      <div className="w-80 bg-white border-r border-gray-200 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="w-80 bg-white border-r border-gray-200 flex flex-col h-full">
      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Chat</h2>
        
        {/* Search */}
        <input
          type="text"
          placeholder="Search users..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-sm"
        />

        {/* Role Filter */}
        <div className="flex gap-2 mt-3">
          {(['ALL', 'EMPLOYEE', 'MANAGER', 'ADMIN'] as const).map((role) => (
            <button
              key={role}
              onClick={() => setFilterRole(role)}
              className={`px-3 py-1 text-xs rounded-full transition-colors ${
                filterRole === role
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {role}
            </button>
          ))}
        </div>
      </div>

      {/* User List */}
      <div className="flex-1 overflow-y-auto">
        {filteredUsers.length === 0 ? (
          <div className="p-4 text-center text-gray-500 text-sm">
            No users found
          </div>
        ) : (
          <div className="p-2">
            {/* Admins */}
            {groupedUsers.ADMIN.length > 0 && (
              <div className="mb-4">
                <div className="px-2 py-1 text-xs font-semibold text-gray-500 uppercase mb-2">
                  Admins ({groupedUsers.ADMIN.length})
                </div>
                {groupedUsers.ADMIN.map((user) => (
                  <button
                    key={user.id}
                    onClick={() => onSelectUser(user)}
                    className={`w-full flex items-center space-x-3 px-3 py-2 rounded-lg mb-1 transition-colors ${
                      selectedUserId === user.id
                        ? 'bg-blue-100 text-blue-900'
                        : 'hover:bg-gray-100 text-gray-900'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-white font-medium flex-shrink-0">
                      {user.firstName?.charAt(0) || user.email.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0 text-left">
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-medium truncate">
                          {user.displayName || `${user.firstName} ${user.lastName}` || user.email}
                        </span>
                        {getRoleIcon(user.role)}
                      </div>
                      <div className="text-xs text-gray-500 truncate">{user.email}</div>
                    </div>
                  </button>
                ))}
              </div>
            )}

            {/* Managers */}
            {groupedUsers.MANAGER.length > 0 && (
              <div className="mb-4">
                <div className="px-2 py-1 text-xs font-semibold text-gray-500 uppercase mb-2">
                  Managers ({groupedUsers.MANAGER.length})
                </div>
                {groupedUsers.MANAGER.map((user) => (
                  <button
                    key={user.id}
                    onClick={() => onSelectUser(user)}
                    className={`w-full flex items-center space-x-3 px-3 py-2 rounded-lg mb-1 transition-colors ${
                      selectedUserId === user.id
                        ? 'bg-blue-100 text-blue-900'
                        : 'hover:bg-gray-100 text-gray-900'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium flex-shrink-0">
                      {user.firstName?.charAt(0) || user.email.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0 text-left">
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-medium truncate">
                          {user.displayName || `${user.firstName} ${user.lastName}` || user.email}
                        </span>
                        {getRoleIcon(user.role)}
                      </div>
                      <div className="text-xs text-gray-500 truncate">{user.email}</div>
                    </div>
                  </button>
                ))}
              </div>
            )}

            {/* Employees */}
            {groupedUsers.EMPLOYEE.length > 0 && (
              <div className="mb-4">
                <div className="px-2 py-1 text-xs font-semibold text-gray-500 uppercase mb-2">
                  Employees ({groupedUsers.EMPLOYEE.length})
                </div>
                {groupedUsers.EMPLOYEE.map((user) => (
                  <button
                    key={user.id}
                    onClick={() => onSelectUser(user)}
                    className={`w-full flex items-center space-x-3 px-3 py-2 rounded-lg mb-1 transition-colors ${
                      selectedUserId === user.id
                        ? 'bg-blue-100 text-blue-900'
                        : 'hover:bg-gray-100 text-gray-900'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-full bg-gray-600 flex items-center justify-center text-white font-medium flex-shrink-0">
                      {user.firstName?.charAt(0) || user.email.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0 text-left">
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-medium truncate">
                          {user.displayName || `${user.firstName} ${user.lastName}` || user.email}
                        </span>
                        {getRoleIcon(user.role)}
                      </div>
                      <div className="text-xs text-gray-500 truncate">{user.email}</div>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

