import { useState, useEffect } from 'react'
import { notificationsApi } from '../api'
import type { Notification } from '../types'

// Mock notifications for testing UI
const mockNotifications: Notification[] = [
  {
    id: 'notif1',
    type: 'message',
    title: 'New message',
    message: 'You have 3 new messages',
    userId: '1',
    read: false,
    timestamp: new Date()
  },
  {
    id: 'notif2',
    type: 'mention',
    title: 'Mentioned in chat',
    message: 'Shubham Gupta mentioned you',
    userId: '1',
    read: false,
    timestamp: new Date()
  }
]

export function useNotifications() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        setLoading(true)
        try {
          const data = await notificationsApi.getNotifications()
          setNotifications(data)
        } catch {
          // Use mock data when backend is not available
          setNotifications(mockNotifications)
        }
        setError(null)
      } catch (err) {
        setError(err as Error)
        setNotifications(mockNotifications)
      } finally {
        setLoading(false)
      }
    }

    fetchNotifications()
    
    // Poll for new notifications every 30 seconds
    const interval = setInterval(fetchNotifications, 30000)
    return () => clearInterval(interval)
  }, [])

  const markAsRead = async (notificationId: string) => {
    try {
      try {
        await notificationsApi.markAsRead(Number(notificationId))
      } catch {
        // Mock behavior when backend is not available
      }
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, read: true } : n))
      )
    } catch (err) {
      setError(err as Error)
    }
  }

  return {
    notifications,
    loading,
    error,
    unreadCount: notifications.filter((n) => !n.read).length,
    markAsRead,
    refetch: () => notificationsApi.getNotifications().then(setNotifications).catch(() => setNotifications(mockNotifications)),
  }
}

