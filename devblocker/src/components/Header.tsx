import { Link, useLocation } from 'react-router-dom'
import { useState } from 'react'
import type { User, Settings, Notification } from '../types'

interface HeaderProps {
  searchQuery?: string
  onSearchChange?: (query: string) => void
  unreadNotifications?: number
  currentUser?: User
  onProfileClick?: () => void
  settings?: Settings
  onSettingsChange?: (settings: Settings) => void
}

function Header({ 
  onProfileClick = () => {}, 
  unreadNotifications = 0,
  currentUser,
  onSettingsChange = () => {}
}: HeaderProps) {
  const location = useLocation()
  const isTeamsPage = location.pathname === '/' || location.pathname === '/teams'
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const [showNotifications, setShowNotifications] = useState(false)
  
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-30 h-14 flex items-center px-4 sm:px-6 lg:px-8">
      {isTeamsPage ? (
        <>
          {/* Teams Free Landing Page Header */}
          <div className="flex items-center justify-between w-full">
            <div className="flex items-center space-x-8">
              <Link to="/" className="text-xl font-semibold text-gray-900">
                Teams meetings
              </Link>
              <div className="hidden md:flex items-center space-x-6">
                <a href="#" className="text-gray-700 hover:text-gray-900 font-medium">
                  Download Teams
                </a>
              </div>
            </div>
            
            <div className="flex items-center space-x-4">
              <button className="text-gray-700 hover:text-gray-900 font-medium hidden sm:block">
                Sign in
              </button>
              <button className="bg-blue-600 text-white px-4 py-2 rounded-md font-medium hover:bg-blue-700 transition-colors">
                Start a meeting
              </button>
              <button className="text-gray-700 hover:text-gray-900 font-medium">
                Download the Teams app
              </button>
            </div>
          </div>

          {/* Action Buttons - Hidden on landing page, shown when in chat */}
          <div className="flex items-center space-x-1 hidden">
            {/* Notifications */}
            <div className="relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded hover:bg-gray-100 text-gray-700"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                {unreadNotifications > 0 && (
                  <span className="absolute top-1 right-1 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center text-[10px]">
                    {unreadNotifications > 9 ? '9+' : unreadNotifications}
                  </span>
                )}
              </button>
              
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 max-h-96 overflow-y-auto z-50">
                  <div className="p-4 border-b border-gray-200">
                    <h3 className="font-semibold text-gray-900">Notifications</h3>
                  </div>
                  <div className="p-2">
                    {unreadNotifications === 0 ? (
                      <p className="text-center py-4 text-gray-500">No new notifications</p>
                    ) : (
                      <p className="text-center py-4 text-gray-500">Notifications will appear here</p>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* User Profile */}
            {currentUser && (
              <div className="relative">
                <button
                  onClick={() => {
                    setShowProfileMenu(!showProfileMenu)
                    onProfileClick()
                  }}
                  className="p-1 rounded hover:bg-gray-100"
                >
                  <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-xs font-semibold">
                    {currentUser.avatar ? (
                      <img src={currentUser.avatar} alt={currentUser.name} className="w-full h-full rounded-full" />
                    ) : (
                      <span>{currentUser.name.charAt(0).toUpperCase()}</span>
                    )}
                  </div>
                </button>

                {showProfileMenu && (
                  <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-xl border border-gray-200 py-2 z-50">
                    <div className="px-4 py-3 border-b border-gray-200">
                      <p className="font-semibold text-gray-900">{currentUser.name}</p>
                      <p className="text-sm text-gray-500">{currentUser.email}</p>
                    </div>
                    <button className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700">
                      View Profile
                    </button>
                    <button className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700">
                      Status: {currentUser.status.charAt(0).toUpperCase() + currentUser.status.slice(1)}
                    </button>
                    <button 
                      onClick={() => {
                        if (settings && onSettingsChange) {
                          onSettingsChange({
                            ...settings,
                            theme: { mode: settings.theme.mode === 'light' ? 'dark' : 'light' }
                          })
                        }
                      }}
                      className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                    >
                      {settings?.theme.mode === 'dark' ? '☀️ Light Theme' : '🌙 Dark Theme'}
                    </button>
                    <button className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700">
                      Settings
                    </button>
                    <button className="w-full text-left px-4 py-2 hover:bg-gray-100 text-red-600">
                      Sign out
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      ) : (
        <div className="flex items-center space-x-4 flex-1">
          <Link to="/" className="text-xl font-semibold text-blue-600">
            DevBlocker
          </Link>
        </div>
      )}
    </header>
  )
}

export default Header

