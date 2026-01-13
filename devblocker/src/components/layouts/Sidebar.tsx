/**
 * Sidebar Component
 * Navigation sidebar similar to Microsoft Teams
 */

import React from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export const Sidebar: React.FC = () => {
  const location = useLocation();
  const { role } = useAuth();

  const navItems = [
    { path: '/app/teams', icon: '👥', label: 'Teams' },
    { path: '/app/chat', icon: '💬', label: 'Chat' },
    { path: '/app/meetings', icon: '📹', label: 'Meetings' },
    { path: '/app/tasks', icon: '📋', label: 'Tasks' },
    { path: '/app/files', icon: '📁', label: 'Files' },
    { path: '/app/notifications', icon: '🔔', label: 'Notifications' },
  ];

  // Admin-specific items
  if (role === 'ADMIN') {
    navItems.push({ path: '/app/admin', icon: '⚙️', label: 'Admin' });
  }

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
      {/* Logo/Header */}
      <div className="p-4 border-b border-gray-200">
        <h1 className="text-xl font-bold text-blue-600">DevBlocker</h1>
        <p className="text-xs text-gray-500">Teams & Jira Platform</p>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto p-2">
        <ul className="space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <li key={item.path}>
                <NavLink
                  to={item.path}
                  className={`
                    flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors
                    ${isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-700 hover:bg-gray-50'
                    }
                  `}
                >
                  <span className="mr-3 text-xl">{item.icon}</span>
                  <span>{item.label}</span>
                </NavLink>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* User Section */}
      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center">
          <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-medium">
            {role?.charAt(0) || 'U'}
          </div>
          <div className="ml-3 flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate">User</p>
            <p className="text-xs text-gray-500 truncate">{role || 'Unknown'}</p>
          </div>
        </div>
      </div>
    </aside>
  );
};

