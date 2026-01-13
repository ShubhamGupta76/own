import { useState } from 'react'
import type { Meeting, Participant } from '../types'

interface JoinMeetingProps {
  onJoinMeeting?: (meeting: { id: string; passcode: string }) => void
  onClose?: () => void
  onMute?: () => void
  onVideoToggle?: () => void
  onScreenShare?: () => void
  onLeave?: () => void
}

function JoinMeeting({ onJoinMeeting, onClose, onMute, onVideoToggle, onScreenShare, onLeave }: JoinMeetingProps) {
  const [meetingId, setMeetingId] = useState('')
  const [passcode, setPasscode] = useState('')
  const [isInMeeting, setIsInMeeting] = useState(false)
  const [isMuted, setIsMuted] = useState(false)
  const [isVideoOn, setIsVideoOn] = useState(true)
  const [isScreenSharing, setIsScreenSharing] = useState(false)
  const [showParticipants, setShowParticipants] = useState(false)

  const mockParticipants: Participant[] = [
    { id: '1', name: 'John Doe', email: 'john@example.com' },
    { id: '2', name: 'Jane Smith', email: 'jane@example.com' },
    { id: '3', name: 'Bob Johnson', email: 'bob@example.com' }
  ]

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (onJoinMeeting) {
      onJoinMeeting({ id: meetingId, passcode })
    }
    setIsInMeeting(true)
  }

  const handleMute = () => {
    setIsMuted(!isMuted)
    if (onMute) onMute()
  }

  const handleVideoToggle = () => {
    setIsVideoOn(!isVideoOn)
    if (onVideoToggle) onVideoToggle()
  }

  const handleScreenShare = () => {
    setIsScreenSharing(!isScreenSharing)
    if (onScreenShare) onScreenShare()
  }

  const handleLeave = () => {
    setIsInMeeting(false)
    if (onLeave) onLeave()
  }

  if (!isInMeeting) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8 max-w-md w-full mx-4">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Join Meeting</h2>
            {onClose && (
              <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="meetingId" className="block text-sm font-medium text-gray-700 mb-2">
                Meeting ID <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="meetingId"
                value={meetingId}
                onChange={(e) => setMeetingId(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                placeholder="Enter meeting ID"
                required
                pattern="[A-Za-z0-9]+"
              />
            </div>
            <div>
              <label htmlFor="passcode" className="block text-sm font-medium text-gray-700 mb-2">
                Meeting passcode <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="passcode"
                value={passcode}
                onChange={(e) => setPasscode(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                placeholder="Enter passcode"
                required
                pattern="[A-Za-z0-9]+"
              />
            </div>
            <div className="flex space-x-3">
              <button
                type="submit"
                className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-md font-medium hover:bg-blue-700 transition-colors"
              >
                Join meeting
              </button>
              <button
                type="button"
                onClick={onClose}
                className="px-6 py-3 border border-gray-300 rounded-md font-medium hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="fixed inset-0 bg-gray-900 z-50 flex flex-col">
      {/* Participants Grid */}
      <div className="flex-1 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4">
        {mockParticipants.map((participant) => (
          <div key={participant.id} className="relative bg-gray-800 rounded-lg overflow-hidden aspect-video">
            {isVideoOn ? (
              <div className="w-full h-full bg-gradient-to-br from-blue-600 to-purple-600 flex items-center justify-center">
                <span className="text-white text-4xl font-bold">
                  {participant.name.charAt(0).toUpperCase()}
                </span>
              </div>
            ) : (
              <div className="w-full h-full bg-gray-700 flex items-center justify-center">
                <span className="text-white text-2xl">
                  {participant.name.charAt(0).toUpperCase()}
                </span>
              </div>
            )}
            <div className="absolute bottom-2 left-2 bg-black bg-opacity-60 text-white text-sm px-2 py-1 rounded">
              {participant.name}
              {participant.id === '1' && <span className="ml-2 text-green-400">●</span>}
            </div>
            {participant.id === '1' && isMuted && (
              <div className="absolute top-2 left-2 bg-red-600 text-white px-2 py-1 rounded text-xs">
                🔇 Muted
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Participants List Sidebar */}
      {showParticipants && (
        <div className="absolute right-0 top-0 bottom-20 w-64 bg-gray-800 border-l border-gray-700 overflow-y-auto">
          <div className="p-4 border-b border-gray-700 flex items-center justify-between">
            <h3 className="text-white font-semibold">Participants ({mockParticipants.length})</h3>
            <button onClick={() => setShowParticipants(false)} className="text-gray-400 hover:text-white">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div className="p-2">
            {mockParticipants.map((participant) => (
              <div key={participant.id} className="p-2 hover:bg-gray-700 rounded flex items-center space-x-3">
                <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-sm">
                  {participant.name.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1">
                  <p className="text-white text-sm">{participant.name}</p>
                  <p className="text-gray-400 text-xs">{participant.email}</p>
                </div>
                {participant.id === '1' && <span className="text-green-400 text-xs">●</span>}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Meeting Controls */}
      <div className="bg-gray-800 border-t border-gray-700 px-6 py-4">
        <div className="flex items-center justify-center space-x-4">
          <button
            onClick={handleMute}
            className={`p-3 rounded-full ${isMuted ? 'bg-red-600' : 'bg-gray-700'} text-white hover:bg-opacity-80 transition-colors`}
            title={isMuted ? 'Unmute' : 'Mute'}
          >
            {isMuted ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2" />
              </svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
              </svg>
            )}
          </button>

          <button
            onClick={handleVideoToggle}
            className={`p-3 rounded-full ${isVideoOn ? 'bg-gray-700' : 'bg-red-600'} text-white hover:bg-opacity-80 transition-colors`}
            title={isVideoOn ? 'Turn off video' : 'Turn on video'}
          >
            {isVideoOn ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
              </svg>
            )}
          </button>

          <button
            onClick={handleScreenShare}
            className={`p-3 rounded-full ${isScreenSharing ? 'bg-blue-600' : 'bg-gray-700'} text-white hover:bg-opacity-80 transition-colors`}
            title={isScreenSharing ? 'Stop sharing' : 'Share screen'}
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </button>

          <button
            onClick={() => setShowParticipants(!showParticipants)}
            className="p-3 rounded-full bg-gray-700 text-white hover:bg-opacity-80 transition-colors"
            title="Participants"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </button>

          <button
            onClick={handleLeave}
            className="px-6 py-3 rounded-full bg-red-600 text-white hover:bg-red-700 transition-colors font-medium"
            title="Leave meeting"
          >
            Leave
          </button>
        </div>
      </div>
    </div>
  )
}

export default JoinMeeting

