import { useState, useEffect } from 'react'
import { chatApi } from '../api'
import type { Chat, Message } from '../types'

// Mock data for testing UI
const mockChats: Chat[] = [
  {
    id: '1',
    type: 'direct',
    name: 'Shubham Gupta (You)',
    participants: ['1'],
    unreadCount: 0,
    isFavorite: true,
    lastMessage: {
      id: 'msg1',
      chatId: '1',
      senderId: '1',
      content: 'like Postman',
      timestamp: new Date('2025-10-24T22:46:00'),
      status: 'read'
    } as Message
  },
  {
    id: '2',
    type: 'group',
    name: 'The Beyonders-EOD',
    participants: ['1', '2', '3'],
    unreadCount: 3,
    icon: '📅',
    lastMessage: {
      id: 'msg2',
      chatId: '2',
      senderId: '2',
      content: 'Daily standup in 10 minutes',
      timestamp: new Date(),
      status: 'read'
    } as Message
  },
  {
    id: '3',
    type: 'group',
    name: 'FalconZ',
    participants: ['1', '2', '3', '4'],
    unreadCount: 0,
    icon: '🐦',
    lastMessage: {
      id: 'msg3',
      chatId: '3',
      senderId: '3',
      content: 'Great work team!',
      timestamp: new Date(),
      status: 'read'
    } as Message
  },
  {
    id: '4',
    type: 'group',
    name: 'The Beyonders DSM',
    participants: ['1', '2'],
    unreadCount: 1,
    icon: '📅',
    lastMessage: {
      id: 'msg4',
      chatId: '4',
      senderId: '2',
      content: 'Meeting notes attached',
      timestamp: new Date(),
      status: 'read'
    } as Message
  },
  {
    id: '5',
    type: 'group',
    name: 'CN Org',
    participants: ['1', '2', '3', '4', '5'],
    unreadCount: 0,
    icon: '☁️',
    lastMessage: {
      id: 'msg5',
      chatId: '5',
      senderId: '4',
      content: 'Updated deployment pipeline',
      timestamp: new Date(),
      status: 'read'
    } as Message
  },
  {
    id: '6',
    type: 'direct',
    name: 'Vasu Sethiya',
    participants: ['1', '6'],
    unreadCount: 2,
    lastMessage: {
      id: 'msg6',
      chatId: '6',
      senderId: '6',
      content: 'Can we schedule a call?',
      timestamp: new Date(),
      status: 'delivered'
    } as Message
  },
  {
    id: '7',
    type: 'group',
    name: 'Development-client\'s discussion',
    participants: ['1', '2', '3', '4'],
    unreadCount: 0,
    lastMessage: {
      id: 'msg7',
      chatId: '7',
      senderId: '2',
      content: 'Client approved the changes',
      timestamp: new Date(),
      status: 'read'
    } as Message
  }
]

export function useChats(filter?: 'unread' | 'channels' | 'chats' | 'meeting') {
  const [chats, setChats] = useState<Chat[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    const fetchChats = async () => {
      try {
        setLoading(true)
        try {
          const data = await chatApi.getChats(filter)
          setChats(data)
        } catch {
          // Use mock data when backend is not available
          let filtered = [...mockChats]
          if (filter === 'unread') {
            filtered = filtered.filter(c => c.unreadCount > 0)
          } else if (filter === 'channels') {
            filtered = filtered.filter(c => c.type === 'channel')
          } else if (filter === 'chats') {
            filtered = filtered.filter(c => c.type === 'direct' || c.type === 'group')
          } else if (filter === 'meeting') {
            filtered = filtered.filter(c => c.type === 'meeting')
          }
          setChats(filtered)
        }
        setError(null)
      } catch (err) {
        setError(err as Error)
        setChats(mockChats)
      } finally {
        setLoading(false)
      }
    }

    fetchChats()
  }, [filter])

  return { chats, loading, error, refetch: () => chatApi.getChats(filter).then(setChats).catch(() => setChats(mockChats)) }
}

export function useFavoriteChats() {
  const [chats, setChats] = useState<Chat[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        setLoading(true)
        try {
          const data = await chatApi.getFavoriteChats()
          setChats(data)
        } catch {
          // Use mock data when backend is not available
          setChats(mockChats.filter(c => c.isFavorite))
        }
        setError(null)
      } catch (err) {
        setError(err as Error)
        setChats(mockChats.filter(c => c.isFavorite))
      } finally {
        setLoading(false)
      }
    }

    fetchFavorites()
  }, [])

  return { chats, loading, error }
}

