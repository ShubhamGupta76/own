/**
 * Notifications Page
 * View all notifications and activity feed
 */

import React, { useState, useEffect } from 'react';
import { notificationsApi } from '../api';
import { wsManager } from '../services/websocket';
import { useAuth } from '../contexts/AuthContext';
import type { Notification, NotificationEvent } from '../types/api';

export const NotificationsPage: React.FC = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState<'all' | 'unread' | 'activity'>('all');

  useEffect(() => {
    // Skip if user doesn't have organizationId
    if (!user?.organizationId || user.organizationId === 0) {
      setError('Please create an organization first to view notifications.');
      setIsLoading(false);
      return;
    }

    fetchNotifications();

    // Connect to notifications WebSocket (only if organizationId exists)
    wsManager.connectToNotifications((event: NotificationEvent) => {
      if (event.type === 'NOTIFICATION_RECEIVED') {
        setNotifications((prev) => [event.notification, ...prev]);
        if (!event.notification.read) {
          setUnreadCount((prev) => prev + 1);
        }
      }
    });

    return () => {
      wsManager.disconnectFromNotifications();
    };
  }, [filter, user?.organizationId]);

  const fetchNotifications = async () => {
    // Skip if no organizationId
    if (!user?.organizationId || user.organizationId === 0) {
      return;
    }

    setIsLoading(true);
    setError('');
    try {
      let data: Notification[];
      if (filter === 'unread') {
        data = await notificationsApi.getUnreadNotifications();
      } else if (filter === 'activity') {
        data = await notificationsApi.getActivityFeed(50);
      } else {
        data = await notificationsApi.getNotifications();
      }

      setNotifications(data);
      
      if (filter === 'all') {
        const count = await notificationsApi.getUnreadCount();
        setUnreadCount(count);
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message ||
                          'Failed to load notifications';
      
      if (errorMessage.includes('Organization not found') || errorMessage.includes('organization')) {
        setError('Please create an organization first to view notifications.');
      } else {
        setError(errorMessage);
      }
      console.error('Error fetching notifications:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      await notificationsApi.markAsRead(notificationId);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err: any) {
      console.error('Error marking notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationsApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err: any) {
      console.error('Error marking all as read:', err);
    }
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'MENTION':
        return '👤';
      case 'TASK':
        return '📋';
      case 'FILE':
        return '📁';
      case 'MEETING':
        return '📹';
      case 'MESSAGE':
        return '💬';
      default:
        return '🔔';
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          <p className="text-gray-600 mt-1">
            {filter === 'unread' ? 'Unread notifications' : filter === 'activity' ? 'Activity feed' : 'All notifications'}
          </p>
        </div>
        <div className="flex items-center space-x-4">
          {filter === 'all' && unreadCount > 0 && (
            <button
              onClick={handleMarkAllAsRead}
              className="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Mark all as read
            </button>
          )}
        </div>
      </div>

      {/* Error Message - Show early if organization missing */}
      {error && error.includes('organization') ? (
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="max-w-md text-center">
            <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-6 py-4 rounded-lg">
              <h3 className="text-lg font-semibold mb-2">Organization Required</h3>
              <p className="text-sm">{error}</p>
            </div>
          </div>
        </div>
      ) : (
        <>
          {/* Filter Tabs */}
          <div className="flex space-x-4 border-b border-gray-200">
            <button
              onClick={() => setFilter('all')}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                filter === 'all'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setFilter('unread')}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors relative ${
                filter === 'unread'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Unread
              {unreadCount > 0 && (
                <span className="ml-2 px-2 py-0.5 text-xs bg-red-500 text-white rounded-full">
                  {unreadCount}
                </span>
              )}
            </button>
            <button
              onClick={() => setFilter('activity')}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                filter === 'activity'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Activity
            </button>
          </div>

          {/* Other Error Messages */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Notifications List */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        {notifications.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">🔔</div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">No Notifications</h2>
            <p className="text-gray-600">
              {filter === 'unread'
                ? 'You have no unread notifications'
                : filter === 'activity'
                ? 'No activity to show'
                : 'You have no notifications yet'}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`p-6 hover:bg-gray-50 transition-colors cursor-pointer ${
                  !notification.read ? 'bg-blue-50' : ''
                }`}
                onClick={() => handleMarkAsRead(notification.id)}
              >
                <div className="flex items-start space-x-4">
                  <div className="text-2xl flex-shrink-0">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="text-sm font-semibold text-gray-900">{notification.title}</h3>
                      {!notification.read && (
                        <span className="w-2 h-2 bg-blue-600 rounded-full flex-shrink-0"></span>
                      )}
                    </div>
                    <p className="text-sm text-gray-600 mb-2">{notification.message}</p>
                    <div className="flex items-center space-x-4 text-xs text-gray-500">
                      <span>{new Date(notification.createdAt).toLocaleString()}</span>
                      {notification.targetEntityType && (
                        <span className="px-2 py-1 bg-gray-100 rounded">
                          {notification.targetEntityType}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
        </>
      )}
    </div>
  );
};

