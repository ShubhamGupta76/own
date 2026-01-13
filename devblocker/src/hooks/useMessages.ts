import { useState, useEffect } from 'react'
import { chatApi } from '../api'
import type { Message } from '../types'

// Mock messages for testing UI
const mockMessages: Record<string, Message[]> = {
  '1': [
    {
      id: 'msg1',
      chatId: '1',
      senderId: '1',
      content: 'like Postman',
      timestamp: new Date('2025-10-24T22:46:00'),
      status: 'read'
    },
    {
      id: 'msg2',
      chatId: '1',
      senderId: '1',
      content: 'Docker',
      timestamp: new Date('2025-10-24T22:57:00'),
      status: 'read'
    },
    {
      id: 'msg3',
      chatId: '1',
      senderId: '1',
      content: 'bruno this is a tool test the end points like a user',
      timestamp: new Date('2025-10-24T21:36:00'),
      status: 'read'
    },
    {
      id: 'msg4',
      chatId: '1',
      senderId: '1',
      content: 'EC2 ECS managing S3 Bucket Lambda AWS services',
      timestamp: new Date('2025-10-24T22:37:00'),
      status: 'read'
    },
    {
      id: 'msg5',
      chatId: '1',
      senderId: '1',
      content: `Monitor CloudWatch & Splunk logs → look for ingestion or rule engine failures.\nReview SNS-SQS queues → verify message flow and DLQ counts.\nRun Splunk queries → confirm "Travel order saved in ee-ingestion-recon".`,
      timestamp: new Date('2025-10-29T22:21:00'),
      status: 'read'
    },
    {
      id: 'msg6',
      chatId: '1',
      senderId: '1',
      content: 'https://netflixmovie-gshubhamkumar01-8430s-projects.vercel.app/',
      timestamp: new Date('2025-10-29T01:28:00'),
      status: 'read'
    }
  ]
}

export function useMessages(chatId: string | null) {
  const [messages, setMessages] = useState<Message[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    if (!chatId) {
      setMessages([])
      return
    }

    const fetchMessages = async () => {
      try {
        setLoading(true)
        try {
          const data = await chatApi.getChannelMessages(Number(chatId))
          setMessages(data.content || data)
        } catch {
          // Use mock data when backend is not available
          setMessages(mockMessages[chatId] || [])
        }
        setError(null)
      } catch (err) {
        setError(err as Error)
        setMessages(mockMessages[chatId] || [])
      } finally {
        setLoading(false)
      }
    }

    fetchMessages()
  }, [chatId])

  const sendMessage = async (content: string, attachments?: File[]) => {
    if (!chatId) return

    try {
      try {
        const newMessage = await chatApi.sendMessage({
          chatRoomId: Number(chatId),
          content,
          messageType: 'TEXT',
        })
        setMessages((prev) => [...prev, newMessage])
        return newMessage
      } catch {
        // Use mock data when backend is not available
        const newMessage: Message = {
          id: Date.now().toString(),
          chatId,
          senderId: '1',
          content,
          timestamp: new Date(),
          status: 'sent',
          attachments: attachments?.map((file, idx) => ({
            id: `att${Date.now()}-${idx}`,
            name: file.name,
            size: file.size,
            type: file.type,
            url: URL.createObjectURL(file),
            uploadedAt: new Date()
          }))
        }
        setMessages((prev) => [...prev, newMessage])
        if (!mockMessages[chatId]) {
          mockMessages[chatId] = []
        }
        mockMessages[chatId].push(newMessage)
        return newMessage
      }
    } catch (err) {
      setError(err as Error)
      throw err
    }
  }

  return { messages, loading, error, sendMessage, refetch: () => chatId && (chatApi.getChannelMessages(Number(chatId)).then((data) => setMessages(data.content || data)).catch(() => setMessages(mockMessages[chatId] || []))) }
}

