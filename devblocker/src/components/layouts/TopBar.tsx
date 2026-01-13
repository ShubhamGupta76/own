/**
 * Top Bar Component
 * Header with team/channel context, tabs (Chat, Files, Tasks, Meetings), search, and profile menu
 * Similar to Microsoft Teams top bar
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { HiSearch, HiOutlineBell, HiOutlineCog, HiLogout } from 'react-icons/hi';
import { useAuth } from '../../contexts/AuthContext';
import { useTeam } from '../../contexts/TeamContext';
import { notificationsApi } from '../../api';
import { wsManager } from '../../services/websocket';
import type { Notification } from '../../types/api';

type TabType = 'chat' | 'files' | 'tasks' | 'meetings';

export const TopBar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { teamId, channelId } = useParams<{ teamId?: string; channelId?: string }>();
  const { user, logout, role } = useAuth();
  const { selectedTeam, selectedChannel } = useTeam();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<TabType>('chat');

  // Determine active tab from URL
  useEffect(() => {
    if (location.pathname.includes('/files')) {
      setActiveTab('files');
    } else if (location.pathname.includes('/tasks')) {
      setActiveTab('tasks');
    } else if (location.pathname.includes('/meetings')) {
      setActiveTab('meetings');
    } else {
      setActiveTab('chat');
    }
  }, [location.pathname]);

  // Fetch notifications on mount
  useEffect(() => {
    const fetchNotifications = async () => {
      if (!user?.organizationId || user.organizationId === 0) {
        return;
      }

      try {
        const unread = await notificationsApi.getUnreadNotifications();
        const count = await notificationsApi.getUnreadCount();
        setNotifications(unread);
        setUnreadCount(count);
      } catch (error) {
        console.warn('Could not fetch notifications:', error);
      }
    };

    fetchNotifications();

    if (user?.organizationId && user.organizationId > 0) {
      wsManager.connectToNotifications((event) => {
        if (event.type === 'NOTIFICATION_RECEIVED') {
          setNotifications((prev) => [event.notification, ...prev]);
          setUnreadCount((prev) => prev + 1);
        }
      });
    }

    return () => {
      wsManager.disconnectFromNotifications();
    };
  }, [user?.organizationId]);

  // Handle tab navigation
  const handleTabClick = (tab: TabType) => {
    setActiveTab(tab);
    if (teamId && channelId) {
      // Navigate within channel context (only navigate if tab is different)
      const currentTab = location.pathname.split('/').pop();
      if (currentTab !== tab && currentTab !== channelId) {
        navigate(`/app/teams/${teamId}/channels/${channelId}/${tab}`);
      }
    } else {
      // Navigate to top-level tab
      navigate(`/app/${tab}`);
    }
  };

  // Handle logout
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Handle mark notification as read
  const handleMarkAsRead = async (notificationId: number) => {
    try {
      await notificationsApi.markAsRead(notificationId);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  // Get current context (Team / Channel name)
  const getContextTitle = () => {
    if (selectedChannel && selectedTeam) {
      return `${selectedTeam.name} > ${selectedChannel.name}`;
    }
    if (selectedTeam) {
      return selectedTeam.name;
    }
    // Get title from current route
    if (location.pathname.includes('/chat')) return 'Chat';
    if (location.pathname.includes('/meetings')) return 'Meetings';
    if (location.pathname.includes('/tasks')) return 'Tasks';
    if (location.pathname.includes('/files')) return 'Files';
    if (location.pathname.includes('/activity')) return 'Activity';
    if (location.pathname.includes('/settings')) return 'Settings';
    return 'DevBlocker';
  };

  // Show tabs only if in channel context
  const showTabs = Boolean(teamId && channelId);

  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 shrink-0">
      {/* Left Section: Context Title */}
      <div className="flex items-center space-x-4 flex-1 min-w-0">
        <h2 className="text-lg font-semibold text-gray-900 truncate">
          {getContextTitle()}
        </h2>

        {/* Tabs (only show in channel context) */}
        {showTabs && (
          <div className="flex items-center space-x-1 border-l border-gray-200 pl-4 ml-4">
            <button
              onClick={() => handleTabClick('chat')}
              className={`
                px-3 py-1.5 text-sm font-medium rounded transition-colors
                ${activeTab === 'chat'
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
                }
              `}
            >
              Chat
            </button>
            <button
              onClick={() => handleTabClick('files')}
              className={`
                px-3 py-1.5 text-sm font-medium rounded transition-colors
                ${activeTab === 'files'
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
                }
              `}
            >
              Files
            </button>
            <button
              onClick={() => handleTabClick('tasks')}
              className={`
                px-3 py-1.5 text-sm font-medium rounded transition-colors
                ${activeTab === 'tasks'
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
                }
              `}
            >
              Tasks
            </button>
            <button
              onClick={() => handleTabClick('meetings')}
              className={`
                px-3 py-1.5 text-sm font-medium rounded transition-colors
                ${activeTab === 'meetings'
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
                }
              `}
            >
              Meetings
            </button>
          </div>
        )}
      </div>

      {/* Right Section: Search, Notifications, Profile */}
      <div className="flex items-center space-x-2">
        {/* Search */}
        <div className="relative">
          <HiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search"
            className="pl-10 pr-4 py-1.5 w-64 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
            aria-label="Notifications"
          >
            <HiOutlineBell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute top-0 right-0 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {/* Notifications Dropdown */}
          {showNotifications && (
            <>
              <div
                className="fixed inset-0 z-40"
                onClick={() => setShowNotifications(false)}
              />
              <div className="absolute right-0 mt-2 w-80 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-96 overflow-y-auto">
                <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                  <h3 className="font-semibold text-gray-900">Notifications</h3>
                  <button
                    onClick={async () => {
                      try {
                        await notificationsApi.markAllAsRead();
                        setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
                        setUnreadCount(0);
                      } catch (error) {
                        console.error('Error marking all as read:', error);
                      }
                    }}
                    className="text-sm text-blue-600 hover:text-blue-700"
                  >
                    Mark all read
                  </button>
                </div>
                <div className="divide-y divide-gray-200">
                  {notifications.length === 0 ? (
                    <div className="p-4 text-center text-gray-500 text-sm">
                      No notifications
                    </div>
                  ) : (
                    notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className={`p-4 hover:bg-gray-50 cursor-pointer ${
                          !notification.read ? 'bg-blue-50' : ''
                        }`}
                        onClick={() => handleMarkAsRead(notification.id)}
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <p className="text-sm font-medium text-gray-900">
                              {notification.title}
                            </p>
                            <p className="text-sm text-gray-600 mt-1">{notification.message}</p>
                            <p className="text-xs text-gray-400 mt-1">
                              {new Date(notification.createdAt).toLocaleString()}
                            </p>
                          </div>
                          {!notification.read && (
                            <span className="w-2 h-2 bg-blue-600 rounded-full mt-2"></span>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
                <div className="p-4 border-t border-gray-200 text-center">
                  <button
                    onClick={() => {
                      setShowNotifications(false);
                      navigate('/app/activity');
                    }}
                    className="text-sm text-blue-600 hover:text-blue-700"
                  >
                    View all notifications
                  </button>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Profile Menu */}
        <div className="relative">
          <button
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            className="flex items-center space-x-2 p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
            aria-label="Profile menu"
          >
            <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-medium">
              {user?.email?.charAt(0).toUpperCase() || 'U'}
            </div>
          </button>

          {/* Profile Dropdown */}
          {showProfileMenu && (
            <>
              <div
                className="fixed inset-0 z-40"
                onClick={() => setShowProfileMenu(false)}
              />
              <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                <div className="p-4 border-b border-gray-200">
                  <p className="text-sm font-medium text-gray-900 truncate">{user?.email}</p>
                  <p className="text-xs text-gray-500 mt-1">{role}</p>
                </div>
                <div className="py-2">
                  <button
                    onClick={() => {
                      setShowProfileMenu(false);
                      navigate('/app/settings');
                    }}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 flex items-center space-x-2"
                  >
                    <HiOutlineCog className="w-4 h-4" />
                    <span>Settings</span>
                  </button>
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                  >
                    <HiLogout className="w-4 h-4" />
                    <span>Logout</span>
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
