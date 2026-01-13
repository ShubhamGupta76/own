# WebRTC Video Calling Implementation

## Overview

Real-time audio and video calling using WebRTC with peer-to-peer (mesh) architecture. Media streams directly between participants without backend involvement. WebSocket is used **only** for signaling (OFFER, ANSWER, ICE_CANDIDATE, USER_JOINED, USER_LEFT).

## Architecture

### Components

1. **`useWebRTC` Hook** (`src/hooks/useWebRTC.ts`)
   - Reusable React hook for WebRTC peer connections
   - Manages local/remote media streams
   - Handles peer connection lifecycle
   - Provides controls (mute, video, screen share)

2. **`MeetingRoom` Component** (`src/components/MeetingRoom.tsx`)
   - Teams-like UI with video grid layout
   - Control bar (mute, camera, screen share, leave)
   - Responsive grid (1-3 columns based on participant count)

3. **WebSocket Signaling** (`src/services/websocket.ts`)
   - Extended for meeting signaling
   - Handles OFFER, ANSWER, ICE_CANDIDATE messages
   - Broadcasts USER_JOINED and USER_LEFT events

## WebRTC Configuration

### ICE Servers

**Default (Google STUN - Free, Public):**
```typescript
{
  urls: 'stun:stun.l.google.com:19302'
}
```

**Optional (Metered.ca TURN - Configurable via env vars):**
```typescript
// Requires VITE_TURN_USERNAME and VITE_TURN_CREDENTIAL
{
  urls: 'stun:stun.metered.ca:80'
},
{
  urls: 'turn:turn.metered.ca:80',
  username: process.env.VITE_TURN_USERNAME,
  credential: process.env.VITE_TURN_CREDENTIAL
}
```

### Environment Variables

Create `.env` file in `devblocker/` directory:

```env
# API Gateway Configuration (optional, defaults shown)
VITE_API_GATEWAY_HOST=localhost
VITE_API_GATEWAY_PORT=8100

# Optional: Metered.ca TURN Server Credentials
VITE_TURN_USERNAME=your_username
VITE_TURN_CREDENTIAL=your_credential
```

## WebSocket Signaling Format

### Message Types

1. **OFFER** - Initial connection offer
2. **ANSWER** - Response to offer
3. **ICE_CANDIDATE** - ICE candidate for NAT traversal
4. **USER_JOINED** - Broadcast when user joins
5. **USER_LEFT** - Broadcast when user leaves

### Message Payload Format

```typescript
{
  type: "OFFER" | "ANSWER" | "ICE_CANDIDATE" | "USER_JOINED" | "USER_LEFT",
  meetingId: number,
  senderId: number,
  targetUserId?: number, // Optional - undefined means broadcast
  data: {
    offer?: RTCSessionDescriptionInit,
    answer?: RTCSessionDescriptionInit,
    candidate?: RTCIceCandidateInit
  }
}
```

### WebSocket Endpoint

```
ws://localhost:8100/ws/meeting?token=<JWT_TOKEN>&meetingId=<MEETING_ID>&userId=<USER_ID>
```

### WebSocket Topic

```
/topic/meeting/{meetingId}/signaling
```

## Usage Flow

### 1. Join Meeting

```typescript
// Initialize local stream (camera + microphone)
await initialize(true, true);

// Join meeting with existing participants
await joinMeeting([participantId1, participantId2]);
```

### 2. Handle Signaling

```typescript
// WebSocket receives signaling message
wsManager.connectToMeeting(meetingId, userId, async (message) => {
  await handleSignalingMessage(message);
});
```

### 3. Controls

```typescript
// Toggle microphone
const isMuted = toggleMute();

// Toggle camera
const isVideoEnabled = toggleVideo();

// Screen share
await startScreenShare();
await stopScreenShare();

// Leave meeting
leave();
```

## Features

✅ **Multiple Participants** - Mesh model (peer-to-peer connections)  
✅ **Real-time Audio/Video** - Direct browser-to-browser streaming  
✅ **Screen Sharing** - Share screen with automatic fallback to camera  
✅ **Mute/Video Toggle** - Control microphone and camera  
✅ **Dynamic Join/Leave** - Handle participants joining/leaving gracefully  
✅ **Connection Recovery** - Automatic ICE restart on connection failure  
✅ **Error Handling** - Comprehensive error handling and user feedback  

## UI Features

- **Dark Theme** - Teams-like dark interface
- **Responsive Grid** - 1-3 columns based on participant count
- **Video Placeholders** - Avatar when video is disabled
- **Participant Indicators** - Name, mute status, video status
- **Screen Share Indicator** - Visual indicator when sharing screen
- **Control Bar** - Easy access to all controls

## Backend Requirements

### WebSocket Endpoint

The backend must provide a WebSocket endpoint at:
```
/ws/meeting?token={JWT_TOKEN}&meetingId={MEETING_ID}&userId={USER_ID}
```

### Message Handling

The backend should:
1. Accept STOMP-like or direct JSON messages
2. Broadcast signaling messages to all meeting participants
3. Handle subscription to meeting topic: `/topic/meeting/{meetingId}/signaling`
4. Forward messages in the format specified above

### API Endpoints

- `POST /meetings/{meetingId}/join` - Join meeting (returns participant IDs)
- `POST /meetings/{meetingId}/leave` - Leave meeting
- `GET /meetings/{meetingId}` - Get meeting details (optional)

## Testing

### Local Testing

1. **Single Browser** - Test local video stream
2. **Multiple Browsers** - Test peer-to-peer connections
3. **Different Networks** - Test NAT traversal with TURN servers
4. **Screen Share** - Test screen sharing functionality
5. **Join/Leave** - Test dynamic participant management

### Browser Compatibility

- ✅ Chrome/Edge (Chromium) - Full support
- ✅ Firefox - Full support
- ✅ Safari - Full support (iOS 11+)
- ⚠️ Mobile browsers - Limited (may require native apps)

## Troubleshooting

### Camera/Microphone Not Working

- Check browser permissions
- Ensure HTTPS (or localhost for development)
- Check browser console for errors

### No Remote Video

- Check WebSocket connection
- Verify ICE servers are accessible
- Check NAT traversal (may need TURN server)
- Review browser console for WebRTC errors

### Connection Failures

- Verify signaling messages are being exchanged
- Check ICE candidate exchange
- Ensure TURN server credentials (if using)
- Review network configuration (firewalls, NAT)

## Production Considerations

1. **TURN Server** - Use production TURN server (Twilio, Metered.ca, etc.)
2. **HTTPS Required** - WebRTC requires secure context in production
3. **SFU Option** - Consider SFU (Selective Forwarding Unit) for large meetings
4. **Recording** - Implement server-side recording if needed
5. **Bandwidth** - Monitor and optimize bandwidth usage
6. **Error Monitoring** - Implement error tracking and monitoring

## Future Enhancements

- [ ] SFU support for large meetings (10+ participants)
- [ ] Server-side recording
- [ ] Chat during meeting
- [ ] Meeting notes
- [ ] Waiting room
- [ ] Breakout rooms
- [ ] Background blur/replacement
- [ ] Noise suppression
- [ ] Mobile app support

