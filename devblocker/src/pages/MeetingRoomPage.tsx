/**
 * Meeting Room Page
 * Full-screen video call interface
 * Route: /app/meetings/:meetingId/room
 */

import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { MeetingRoom } from '../components/MeetingRoom';
import { meetingsApi } from '../api';
import { useAuth } from '../contexts/AuthContext';
import type { Meeting } from '../types/api';

export const MeetingRoomPage: React.FC = () => {
  const { meetingId } = useParams<{ meetingId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [participantIds, setParticipantIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const audioOnly = searchParams.get('audioOnly') === 'true';
  const screenShare = searchParams.get('screenShare') === 'true';

  useEffect(() => {
    const loadMeeting = async () => {
      if (!meetingId) {
        setError('Meeting ID is required');
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        
        // Join meeting via API (returns participant IDs)
        try {
          const joinResponse = await meetingsApi.joinMeeting(Number(meetingId));
          setParticipantIds(joinResponse.participantIds || []);
        } catch (joinError: any) {
          // If join fails, try to get meeting details anyway
          console.warn('Join meeting API failed, continuing anyway:', joinError);
          setParticipantIds([]);
        }
        
        // Try to fetch meeting details (optional)
        try {
          const meetingData = await meetingsApi.getMeetingById(Number(meetingId));
          setMeeting(meetingData);
        } catch (fetchError) {
          // Meeting details fetch is optional - we can still join with just meetingId
          console.warn('Could not fetch meeting details:', fetchError);
        }
        
        setIsLoading(false);
      } catch (err: any) {
        console.error('Error loading meeting:', err);
        setError(err.message || 'Failed to load meeting');
        setIsLoading(false);
      }
    };

    loadMeeting();
  }, [meetingId]);

  const handleLeave = async () => {
    // Call leave meeting API
    if (meetingId) {
      try {
        await meetingsApi.leaveMeeting(Number(meetingId));
      } catch (error) {
        console.error('Error leaving meeting:', error);
      }
    }
    
    // Navigate back to meetings page
    navigate('/app/meetings');
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white mx-auto mb-4"></div>
          <p className="text-white">Loading meeting...</p>
        </div>
      </div>
    );
  }

  if (error || !meetingId) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-900">
        <div className="text-center">
          <p className="text-red-500 mb-4">{error || 'Meeting not found'}</p>
          <button
            onClick={() => navigate('/app/meetings')}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Back to Meetings
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen w-screen overflow-hidden">
      <MeetingRoom
        meetingId={Number(meetingId)}
        participantIds={participantIds}
        onLeave={handleLeave}
        audioOnly={audioOnly}
        screenShare={screenShare}
      />
    </div>
  );
};

