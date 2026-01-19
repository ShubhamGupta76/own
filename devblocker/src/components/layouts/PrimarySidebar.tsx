/**
 * Primary Sidebar Component
 * Icon-based navigation similar to Microsoft Teams
 * Fixed width, vertical icons with tooltips
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { 
  HiOutlineUserGroup, 
  HiOutlineChatAlt2, 
  HiOutlineVideoCamera,
  HiOutlineClipboardList,
  HiOutlineFolder,
  HiOutlineBell,
  HiOutlineCog,
  HiOutlineShieldCheck
} from 'react-icons/hi';
import { useAuth } from '../../contexts/AuthContext';
import type { UserRole } from '../../types/api';

interface NavItem {
  path: string;
  icon: React.ReactNode;
  label: string;
  id: string;
  roles?: readonly UserRole[];
}

export const PrimarySidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { role } = useAuth();

  // Navigation items (icon-based) - role-based visibility
  const allNavItems: NavItem[] = [
    { 
      id: 'teams',
      path: '/app/teams', 
      icon: <HiOutlineUserGroup className="w-6 h-6" />, 
      label: 'Teams',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER'] as const,
    },
    { 
      id: 'chat',
      path: '/app/chats', 
      icon: <HiOutlineChatAlt2 className="w-6 h-6" />, 
      label: 'Chat',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] as const,
    },
    { 
      id: 'meetings',
      path: '/app/meetings', 
      icon: <HiOutlineVideoCamera className="w-6 h-6" />, 
      label: 'Meetings',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER'] as const,
    },
    { 
      id: 'tasks',
      path: '/app/tasks', 
      icon: <HiOutlineClipboardList className="w-6 h-6" />, 
      label: 'Tasks',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] as const,
    },
    { 
      id: 'files',
      path: '/app/files', 
      icon: <HiOutlineFolder className="w-6 h-6" />, 
      label: 'Files',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER'] as const,
    },
    { 
      id: 'activity',
      path: '/app/activity', 
      icon: <HiOutlineBell className="w-6 h-6" />, 
      label: 'Activity',
      roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] as const,
    },
  ];

  // Filter nav items based on role
  const navItems = allNavItems.filter((item) => {
    if (!item.roles) return true; // Show if no role restriction
    return role && item.roles.includes(role as any);
  });

  // Admin Dashboard (only for ADMIN)
  const adminDashboardItem: NavItem | null = role === 'ADMIN' ? {
    id: 'admin',
    path: '/app/admin/dashboard',
    icon: <HiOutlineShieldCheck className="w-6 h-6" />,
    label: 'Admin',
    roles: ['ADMIN'] as const,
  } : null;

  // Settings icon (always at bottom, except for EXTERNAL_USER)
  const settingsItem: NavItem | null = role !== 'EXTERNAL_USER' ? {
    id: 'settings',
    path: '/app/settings',
    icon: <HiOutlineCog className="w-6 h-6" />,
    label: 'Settings',
    roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] as const,
  } : null;

  const isActive = (path: string, id: string) => {
    if (id === 'teams') {
      return location.pathname.startsWith('/app/teams') || 
             location.pathname.includes('/channels/');
    }
    if (id === 'chat') {
      return location.pathname.startsWith('/app/chat') || 
             location.pathname.startsWith('/app/chats');
    }
    return location.pathname.startsWith(path);
  };

  return (
    <aside className="w-16 bg-gray-900 flex flex-col items-center py-4 border-r border-gray-800">
      {/* Logo/Brand */}
      <div className="mb-6">
        <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
          <span className="text-white font-bold text-lg">DB</span>
        </div>
      </div>

      {/* Navigation Items */}
      <nav className="flex-1 flex flex-col items-center space-y-2 w-full px-2">
        {navItems.map((item) => {
          const active = isActive(item.path, item.id);
          return (
            <div key={item.id} className="relative group w-full">
              <button
                onClick={() => navigate(item.path)}
                className={`
                  w-full h-12 rounded-lg flex items-center justify-center transition-colors
                  ${active
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                  }
                `}
                title={item.label}
                aria-label={item.label}
              >
                {item.icon}
              </button>
              
              {/* Tooltip */}
              <div className="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-sm rounded opacity-0 group-hover:opacity-100 pointer-events-none whitespace-nowrap z-50 transition-opacity">
                {item.label}
              </div>
            </div>
          );
        })}
        
        {/* Admin Dashboard (separator before admin section) */}
        {adminDashboardItem && (
          <>
            <div className="w-full border-t border-gray-700 my-2" />
            <div className="relative group w-full">
              <button
                onClick={() => navigate(adminDashboardItem.path)}
                className={`
                  w-full h-12 rounded-lg flex items-center justify-center transition-colors
                  ${location.pathname.startsWith(adminDashboardItem.path)
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                  }
                `}
                title={adminDashboardItem.label}
                aria-label={adminDashboardItem.label}
              >
                {adminDashboardItem.icon}
              </button>
              
              {/* Tooltip */}
              <div className="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-sm rounded opacity-0 group-hover:opacity-100 pointer-events-none whitespace-nowrap z-50 transition-opacity">
                {adminDashboardItem.label}
              </div>
            </div>
          </>
        )}
      </nav>

      {/* Settings (at bottom) - Hidden for EXTERNAL_USER */}
      {settingsItem && (
        <div className="relative group w-full px-2">
          <button
            onClick={() => navigate(settingsItem.path)}
            className={`
              w-full h-12 rounded-lg flex items-center justify-center transition-colors
              ${location.pathname.startsWith(settingsItem.path)
                ? 'bg-blue-600 text-white'
                : 'text-gray-400 hover:bg-gray-800 hover:text-white'
              }
            `}
            title={settingsItem.label}
            aria-label={settingsItem.label}
          >
            {settingsItem.icon}
          </button>
          
          {/* Tooltip */}
          <div className="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-sm rounded opacity-0 group-hover:opacity-100 pointer-events-none whitespace-nowrap z-50 transition-opacity">
            {settingsItem.label}
          </div>
        </div>
      )}
    </aside>
  );
};

