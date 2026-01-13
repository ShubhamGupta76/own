/**
 * Meetings Page
 * Manage instant calls and scheduled meetings
 */

import React, { useState } from 'react';

interface MeetingsPageProps {
  channelId?: number;
  teamId?: number;
}
import { useNavigate } from 'react-router-dom';
import { meetingsApi } from '../api';
import { useAuth } from '../contexts/AuthContext';
import type { Meeting, CreateInstantCallRequest, ScheduleMeetingRequest } from '../types/api';

export const MeetingsPage: React.FC<MeetingsPageProps> = ({ channelId }) => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [meetings, setMeetings] = useState<Meeting[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [meetingType, setMeetingType] = useState<'instant' | 'scheduled'>('instant');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Form state for instant call
  const [instantTitle, setInstantTitle] = useState('');
  const [instantDescription, setInstantDescription] = useState('');
  const [instantParticipantIds, setInstantParticipantIds] = useState<number[]>([]);

  // Form state for scheduled meeting
  const [scheduledTitle, setScheduledTitle] = useState('');
  const [scheduledDescription, setScheduledDescription] = useState('');
  const [scheduledStartTime, setScheduledStartTime] = useState('');
  const [scheduledEndTime, setScheduledEndTime] = useState('');
  const [scheduledParticipantIds, setScheduledParticipantIds] = useState<number[]>([]);

  const handleCreateInstantCall = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Check if user has organizationId
    if (!user?.organizationId || user.organizationId === 0) {
      setError('Please create an organization first to create meetings.');
      setShowCreateModal(false);
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const request: CreateInstantCallRequest = {
        title: instantTitle,
        description: instantDescription || undefined,
        participantIds: instantParticipantIds.length > 0 ? instantParticipantIds : [],
      };

      const meeting = await meetingsApi.createInstantCall(request);
      setMeetings((prev) => [meeting, ...prev]);
      setShowCreateModal(false);
      setInstantTitle('');
      setInstantDescription('');
      setInstantParticipantIds([]);
      
      // Navigate to meeting room
      navigate(`/app/meetings/${meeting.id}/room`);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message ||
                          'Failed to create meeting';
      
      if (errorMessage.includes('Organization not found') || errorMessage.includes('organization')) {
        setError('Please create an organization first to create meetings.');
      } else {
        setError(errorMessage);
      }
      console.error('Error creating instant call:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleScheduleMeeting = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Check if user has organizationId
    if (!user?.organizationId || user.organizationId === 0) {
      setError('Please create an organization first to schedule meetings.');
      setShowCreateModal(false);
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const request: ScheduleMeetingRequest = {
        title: scheduledTitle,
        description: scheduledDescription || undefined,
        startTime: scheduledStartTime,
        endTime: scheduledEndTime,
        participantIds: scheduledParticipantIds.length > 0 ? scheduledParticipantIds : [],
      };

      const meeting = await meetingsApi.scheduleMeeting(request);
      setMeetings((prev) => [meeting, ...prev]);
      setShowCreateModal(false);
      setScheduledTitle('');
      setScheduledDescription('');
      setScheduledStartTime('');
      setScheduledEndTime('');
      setScheduledParticipantIds([]);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message ||
                          'Failed to schedule meeting';
      
      if (errorMessage.includes('Organization not found') || errorMessage.includes('organization')) {
        setError('Please create an organization first to schedule meetings.');
      } else {
        setError(errorMessage);
      }
      console.error('Error scheduling meeting:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleJoinMeeting = async (meetingId: number) => {
    try {
      // Navigate directly to meeting room - join will happen there
      navigate(`/app/meetings/${meetingId}/room`);
    } catch (error: any) {
      console.error('Error joining meeting:', error);
      alert(error.response?.data?.error || 'Failed to join meeting');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Meetings</h1>
          <p className="text-gray-600 mt-1">Schedule and join meetings</p>
        </div>
        <button
          onClick={() => {
            if (!user?.organizationId || user.organizationId === 0) {
              setError('Please create an organization first to create meetings.');
              return;
            }
            setShowCreateModal(true);
          }}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          + New Meeting
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-4 py-3 rounded-lg">
          <p className="text-sm">{error}</p>
        </div>
      )}

      {/* Meetings List */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        {meetings.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📹</div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">No Meetings Yet</h2>
            <p className="text-gray-600 mb-6">Create a new meeting to get started</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              Create Meeting
            </button>
          </div>
        ) : (
          <ul className="divide-y divide-gray-200">
            {meetings.map((meeting) => (
              <li key={meeting.id} className="p-6 hover:bg-gray-50 transition-colors">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900">{meeting.title}</h3>
                    {meeting.description && (
                      <p className="text-sm text-gray-600 mt-1">{meeting.description}</p>
                    )}
                    <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                      <span>Status: {meeting.status}</span>
                      {meeting.startTime && (
                        <span>Start: {new Date(meeting.startTime).toLocaleString()}</span>
                      )}
                    </div>
                  </div>
                  {meeting.status === 'LIVE' || meeting.status === 'SCHEDULED' ? (
                    <button
                      onClick={() => handleJoinMeeting(meeting.id)}
                      className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                    >
                      Join
                    </button>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Create Meeting Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-gray-900">Create Meeting</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>

            {/* Meeting Type Toggle */}
            <div className="flex bg-gray-100 rounded-lg p-1 mb-4">
              <button
                type="button"
                onClick={() => setMeetingType('instant')}
                className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                  meetingType === 'instant'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600'
                }`}
              >
                Instant Call
              </button>
              <button
                type="button"
                onClick={() => setMeetingType('scheduled')}
                className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                  meetingType === 'scheduled'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600'
                }`}
              >
                Schedule
              </button>
            </div>

            {/* Form */}
            {meetingType === 'instant' ? (
              <form onSubmit={handleCreateInstantCall} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Title
                  </label>
                  <input
                    type="text"
                    value={instantTitle}
                    onChange={(e) => setInstantTitle(e.target.value)}
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                    placeholder="Quick Standup"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Description (Optional)
                  </label>
                  <textarea
                    value={instantDescription}
                    onChange={(e) => setInstantDescription(e.target.value)}
                    rows={3}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
                    placeholder="Meeting description..."
                  />
                </div>
                <div className="flex space-x-4">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {isLoading ? 'Creating...' : 'Create'}
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleScheduleMeeting} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Title
                  </label>
                  <input
                    type="text"
                    value={scheduledTitle}
                    onChange={(e) => setScheduledTitle(e.target.value)}
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                    placeholder="Sprint Planning"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Description (Optional)
                  </label>
                  <textarea
                    value={scheduledDescription}
                    onChange={(e) => setScheduledDescription(e.target.value)}
                    rows={2}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
                    placeholder="Meeting description..."
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Start Time
                  </label>
                  <input
                    type="datetime-local"
                    value={scheduledStartTime}
                    onChange={(e) => setScheduledStartTime(e.target.value)}
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    End Time
                  </label>
                  <input
                    type="datetime-local"
                    value={scheduledEndTime}
                    onChange={(e) => setScheduledEndTime(e.target.value)}
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                  />
                </div>
                <div className="flex space-x-4">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {isLoading ? 'Scheduling...' : 'Schedule'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

