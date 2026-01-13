// Types for backend integration - ready for microservices data

export interface Meeting {
  id: string
  passcode: string
  title?: string
  duration?: number
  scheduledAt?: string
  participants?: Participant[]
}

export interface Participant {
  id: string
  name: string
  email: string
  avatar?: string
}

export interface Feature {
  id: string
  title: string
  description: string
  icon: string
  enabled: boolean
}

export interface AppConfig {
  maxMeetingDuration: number
  supportedLanguages: string[]
  features: Feature[]
}

export type UserStatus = 'online' | 'away' | 'busy' | 'offline'

export interface User {
  id: string
  name: string
  email: string
  avatar?: string
  status: UserStatus
  lastSeen?: Date
}

export interface Team {
  id: string
  name: string
  description?: string
  avatar?: string
  members: string[]
  channels: Channel[]
  createdBy: string
  createdAt: Date
}

export interface Channel {
  id: string
  teamId: string
  name: string
  description?: string
  type: 'public' | 'private'
  members: string[]
  createdAt: Date
}

export interface Message {
  id: string
  channelId?: string
  chatId?: string
  senderId: string
  content: string
  timestamp: Date
  edited?: boolean
  reactions?: MessageReaction[]
  attachments?: FileAttachment[]
  replyTo?: string
  status?: 'sending' | 'sent' | 'delivered' | 'read'
}

export interface MessageReaction {
  emoji: string
  userIds: string[]
}

export interface FileAttachment {
  id: string
  name: string
  size: number
  type: string
  url: string
  thumbnailUrl?: string
  uploadedAt: Date
}

export interface Chat {
  id: string
  type: 'direct' | 'group' | 'channel' | 'meeting'
  name?: string
  participants: string[]
  lastMessage?: Message
  unreadCount: number
  isFavorite?: boolean
  avatar?: string
  icon?: string
  pinned?: boolean
}

export type SidebarView = 'activity' | 'chat' | 'teams' | 'calendar' | 'calls' | 'files'

export interface Notification {
  id: string
  type: 'message' | 'mention' | 'reaction' | 'team_invite' | 'file_shared'
  title: string
  message: string
  userId: string
  relatedId?: string
  read: boolean
  timestamp: Date
}

export interface Theme {
  mode: 'light' | 'dark'
}

export interface Settings {
  theme: Theme
  notifications: {
    enabled: boolean
    sound: boolean
    desktop: boolean
  }
}

