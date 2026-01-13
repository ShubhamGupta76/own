import { useState, useRef, useEffect } from 'react'
import { useMessages } from '../hooks/useMessages'
import type { Channel, Chat, User, Message, FileAttachment } from '../types'

interface HeroProps {
  channel?: Channel | null
  chat?: Chat | null
  currentUser?: User
  onStartMeeting?: () => void
}

function Hero({ channel, chat, currentUser, onStartMeeting }: HeroProps) {
  const chatId = chat?.id || channel?.id || null
  const { messages, loading: messagesLoading, sendMessage: apiSendMessage } = useMessages(chatId)
  
  const [newMessage, setNewMessage] = useState('')
  const [showEmojiPicker, setShowEmojiPicker] = useState(false)
  const [uploadingFile, setUploadingFile] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const emojis = ['😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳']

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newMessage.trim() || !chatId) return

    try {
      const files = fileInputRef.current?.files ? Array.from(fileInputRef.current.files) : undefined
      await apiSendMessage(newMessage, files)
      setNewMessage('')
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
      setUploadingFile(false)
    } catch (error) {
      console.error('Failed to send message:', error)
    }
  }

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files || files.length === 0 || !chatId) return

    setUploadingFile(true)
    try {
      const fileArray = Array.from(files)
      await apiSendMessage(`📎 ${fileArray.map(f => f.name).join(', ')}`, fileArray)
      e.target.value = ''
    } catch (error) {
      console.error('Failed to upload file:', error)
    } finally {
      setUploadingFile(false)
    }
  }

  const addEmoji = (emoji: string) => {
    setNewMessage(newMessage + emoji)
    setShowEmojiPicker(false)
  }

  const addReaction = (messageId: string, emoji: string) => {
    // TODO: Implement reaction API call
    // For now, this is a placeholder - backend will handle reactions
    console.log('Add reaction:', { messageId, emoji })
  }

  if (!channel && !chat) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-700 mb-2">Select a channel or chat to start messaging</h2>
          <p className="text-gray-500">Choose from the sidebar to begin</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1 flex flex-col bg-white h-full">
      {/* Chat Header - Teams Style */}
      <div className="border-b border-gray-200 px-4 h-12 flex items-center justify-between bg-white">
        <div className="flex items-center space-x-3 flex-1 min-w-0">
          {channel && (
            <>
              <span className="text-gray-500 text-lg">{channel.type === 'private' ? '🔒' : '#'}</span>
              <div className="flex-1 min-w-0">
                <h2 className="font-semibold text-gray-900 truncate">{channel.name}</h2>
              </div>
            </>
          )}
          {chat && (
            <div className="flex items-center space-x-3 flex-1 min-w-0">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white text-xs font-semibold flex-shrink-0">
                {chat.type === 'direct' ? chat.id === '1' ? 'JD' : 'JS' : 'GC'}
              </div>
              <div className="flex-1 min-w-0">
                <h2 className="font-semibold text-gray-900 truncate">
                  {chat.type === 'direct' ? (chat.id === '1' ? 'John Doe' : 'Jane Smith') : 'Group Chat'}
                </h2>
              </div>
            </div>
          )}
        </div>
        <div className="flex items-center space-x-1">
          <button onClick={onStartMeeting} className="p-2 hover:bg-gray-100 rounded text-gray-600" title="Start meeting">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
          </button>
          <button className="p-2 hover:bg-gray-100 rounded text-gray-600" title="More options">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
            </svg>
          </button>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-4 py-2 space-y-1">
        {messagesLoading ? (
          <div className="flex items-center justify-center h-full text-gray-500">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : messages.length === 0 ? (
          <div className="flex items-center justify-center h-full text-gray-500">
            <p>No messages yet. Start the conversation!</p>
          </div>
        ) : (
          messages.map((message) => (
            <div key={message.id} className="flex space-x-2 hover:bg-gray-50 px-4 py-1 group">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white text-xs font-semibold flex-shrink-0">
                {currentUser?.name.charAt(0).toUpperCase() || 'U'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-baseline space-x-2">
                  <span className="font-semibold text-gray-900 text-sm">{currentUser?.name || 'User'}</span>
                  <span className="text-xs text-gray-500">
                    {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                <p className="text-gray-900 text-sm leading-relaxed">{message.content}</p>
                
                {/* File Attachments */}
                {message.attachments && message.attachments.map(attachment => (
                  <div key={attachment.id} className="mt-2 p-3 bg-gray-100 rounded-lg flex items-center space-x-3">
                    <span className="text-2xl">📎</span>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-900">{attachment.name}</p>
                      <p className="text-xs text-gray-500">{(attachment.size / 1024).toFixed(2)} KB</p>
                    </div>
                    <a href={attachment.url} download className="text-blue-600 hover:text-blue-700 text-sm">Download</a>
                  </div>
                ))}

                {/* Reactions */}
                {message.reactions && message.reactions.length > 0 && (
                  <div className="flex flex-wrap gap-1 mt-2">
                    {message.reactions.map((reaction, idx) => (
                      <button
                        key={idx}
                        onClick={() => addReaction(message.id, reaction.emoji)}
                        className="px-2 py-1 bg-gray-100 rounded-full text-sm hover:bg-gray-200"
                      >
                        {reaction.emoji} {reaction.userIds.length}
                      </button>
                    ))}
                  </div>
                )}

                {/* Reaction Button */}
                <button
                  onClick={() => addReaction(message.id, '👍')}
                  className="mt-1 text-xs text-gray-500 opacity-0 group-hover:opacity-100 hover:text-gray-700"
                >
                  Add reaction
                </button>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Message Input - Teams Style */}
      <div className="border-t border-gray-200 px-4 py-2 bg-white">
        <form onSubmit={handleSendMessage} className="flex items-center space-x-2">
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            className="hidden"
            multiple
          />
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="p-2 hover:bg-gray-100 rounded text-gray-500 flex-shrink-0"
            title="Attach file"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
            </svg>
          </button>
          <div className="relative flex-1">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a new message"
              className="w-full px-4 py-2 bg-gray-50 border-0 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500 focus:bg-white text-sm"
            />
            <div className="absolute right-2 top-1.5 flex items-center space-x-1">
              <button
                type="button"
                onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                className="p-1 hover:bg-gray-200 rounded text-gray-500"
                title="Add emoji"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </button>
              {showEmojiPicker && (
                <div className="absolute bottom-full right-0 mb-2 p-3 bg-white border border-gray-200 rounded-lg shadow-lg w-64 h-40 overflow-y-auto grid grid-cols-6 gap-2 z-10">
                  {emojis.map(emoji => (
                    <button
                      key={emoji}
                      type="button"
                      onClick={() => addEmoji(emoji)}
                      className="text-2xl hover:bg-gray-100 rounded p-1"
                    >
                      {emoji}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
          {newMessage.trim() && (
            <button
              type="submit"
              className="p-2 text-blue-600 hover:bg-gray-100 rounded flex-shrink-0"
              title="Send message"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          )}
        </form>
      </div>
    </div>
  )
}

export default Hero

