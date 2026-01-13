/**
 * Home/Landing Page
 * Microsoft Teams Free-style landing page
 * Similar to https://teams.live.com/free
 */

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { 
  HiVideoCamera, 
  HiChatAlt2, 
  HiFolder, 
  HiStar,
  HiShare,
  HiClock,
  HiTranslate,
  HiEmojiHappy,
  HiDesktopComputer,
  HiCamera
} from 'react-icons/hi';

export default function HomePage() {
  const navigate = useNavigate();
  const [meetingId, setMeetingId] = useState('');
  const [meetingPasscode, setMeetingPasscode] = useState('');

  const handleJoinMeeting = async (e: React.FormEvent) => {
    e.preventDefault();
    if (meetingId && meetingPasscode) {
      // Navigate to login first (required for joining meetings)
      // After login, user can join the meeting
      navigate('/login', { 
        state: { 
          from: { pathname: `/app/meetings/${meetingId}/room`, meetingId, passcode: meetingPasscode } 
        } 
      });
    }
  };

  const handleStartMeeting = () => {
    navigate('/login');
  };

  const handleOpenInBrowser = () => {
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-gray-50">
      {/* Header */}
      <header className="border-b border-gray-200 bg-white sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-2">
              <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">DB</span>
              </div>
              <span className="text-xl font-semibold text-gray-900">DevBlocker meetings</span>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={handleOpenInBrowser}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors"
              >
                Sign in
              </button>
              <button
                onClick={handleStartMeeting}
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Start a meeting
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24">
        <div className="text-center mb-12">
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-gray-900 mb-6 leading-tight">
            Video calls with anyone, anytime
          </h1>
          <p className="text-xl md:text-2xl text-gray-600 mb-4">
            Connect and collaborate for free in DevBlocker
          </p>
          <p className="text-xl md:text-2xl text-gray-600 mb-12">
            Connect with anyone, anytime
          </p>
          
          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
            <button
              onClick={handleStartMeeting}
              className="w-full sm:w-auto px-8 py-4 text-lg font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors shadow-lg hover:shadow-xl transform hover:scale-105"
            >
              Start a meeting for free
            </button>
            <button
              onClick={handleOpenInBrowser}
              className="w-full sm:w-auto px-8 py-4 text-lg font-semibold text-gray-700 bg-white border-2 border-gray-300 rounded-lg hover:border-gray-400 hover:bg-gray-50 transition-colors"
            >
              Open DevBlocker in the browser
            </button>
          </div>
          
          <p className="text-sm text-gray-600 mb-8">
            Sign in with your account to meet and chat with anyone on DevBlocker for free.
          </p>

        </div>
        
        {/* Join Meeting Section */}
        <div className="max-w-lg mx-auto bg-white rounded-xl shadow-xl p-8 mb-20 border border-gray-200">
          <h2 className="text-2xl font-semibold text-gray-900 mb-2 text-center">
            Join with a meeting ID
          </h2>
          <p className="text-sm text-gray-500 mb-6 text-center">
            Each field should consist of alphanumeric characters only.
          </p>
          <form onSubmit={handleJoinMeeting} className="space-y-5">
            <div>
              <label htmlFor="meetingId" className="block text-sm font-medium text-gray-700 mb-2">
                Meeting ID *
              </label>
              <input
                type="text"
                id="meetingId"
                value={meetingId}
                onChange={(e) => setMeetingId(e.target.value.replace(/[^a-zA-Z0-9]/g, ''))}
                className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-center text-lg font-mono uppercase tracking-wider"
                placeholder="Enter meeting ID"
                required
                pattern="[a-zA-Z0-9]+"
              />
            </div>
            <div>
              <label htmlFor="meetingPasscode" className="block text-sm font-medium text-gray-700 mb-2">
                Meeting passcode *
              </label>
              <input
                type="text"
                id="meetingPasscode"
                value={meetingPasscode}
                onChange={(e) => setMeetingPasscode(e.target.value.replace(/[^a-zA-Z0-9]/g, ''))}
                className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-center text-lg font-mono"
                placeholder="Enter passcode"
                required
                pattern="[a-zA-Z0-9]+"
              />
            </div>
            <button
              type="submit"
              className="w-full px-6 py-3 text-lg font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors shadow-md hover:shadow-lg"
            >
              Join meeting
            </button>
          </form>
        </div>
      </section>

      {/* Features Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {/* Feature 1 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiShare className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              Share meeting links with anyone on any device
            </h3>
            <p className="text-gray-600">
              Collaborate together on DevBlocker video calls
            </p>
          </div>

          {/* Feature 2 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiClock className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              Meet for free up to 60 mins
            </h3>
            <p className="text-gray-600">
              Instant or schedule ahead
            </p>
          </div>

          {/* Feature 3 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiTranslate className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              View Live captions
            </h3>
            <p className="text-gray-600">
              In over 40 languages
            </p>
          </div>

          {/* Feature 4 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiEmojiHappy className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              Entertain the crowd
            </h3>
            <p className="text-gray-600">
              With interactive emoji
            </p>
          </div>

          {/* Feature 5 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiDesktopComputer className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              Share your screen
            </h3>
            <p className="text-gray-600">
              For live collab and play
            </p>
          </div>

          {/* Feature 6 */}
          <div className="bg-white rounded-xl p-8 shadow-md hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6">
              <HiCamera className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3">
              Choose backgrounds and views to set the scene
            </h3>
            <p className="text-gray-600">
              Customize your meeting experience
            </p>
          </div>
        </div>
      </section>

      {/* Bottom Navigation Section */}
      <section className="bg-white border-t border-gray-200 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div className="group cursor-pointer">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4 group-hover:bg-blue-600 transition-colors">
                <HiVideoCamera className="w-8 h-8 text-blue-600 group-hover:text-white transition-colors" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Video calls</h4>
              <p className="text-sm text-gray-600">Connect face-to-face</p>
            </div>

            <div className="group cursor-pointer">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4 group-hover:bg-blue-600 transition-colors">
                <HiChatAlt2 className="w-8 h-8 text-blue-600 group-hover:text-white transition-colors" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Collaboration tools</h4>
              <p className="text-sm text-gray-600">Share, play, shop together</p>
            </div>

            <div className="group cursor-pointer">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4 group-hover:bg-blue-600 transition-colors">
                <HiFolder className="w-8 h-8 text-blue-600 group-hover:text-white transition-colors" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Files</h4>
              <p className="text-sm text-gray-600">Share documents and more</p>
            </div>

            <div className="group cursor-pointer">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4 group-hover:bg-blue-600 transition-colors">
                <HiStar className="w-8 h-8 text-blue-600 group-hover:text-white transition-colors" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 mb-2">Chat themes</h4>
              <p className="text-sm text-gray-600">Reflect your style</p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-50 border-t border-gray-200 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-sm text-gray-600">
              Free DevBlocker meeting for your own personal business
            </div>
            <div className="flex items-center space-x-6 text-sm text-gray-600">
              <Link to="/login" className="hover:text-gray-900">Sign in</Link>
              <span>•</span>
              <Link to="/register-org" className="hover:text-gray-900">Create organization</Link>
              <span>•</span>
              <button onClick={handleStartMeeting} className="hover:text-gray-900">
                Start a meeting
              </button>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
