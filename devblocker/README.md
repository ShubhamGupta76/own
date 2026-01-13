# DevBlocker Frontend

Microsoft Teams + Jira-like collaboration platform frontend built with React, TypeScript, and Tailwind CSS.

## Tech Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router DOM v7** - Routing
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **WebSocket** - Real-time communication

## Features

- ✅ **Role-based Authentication** (ADMIN, MANAGER, EMPLOYEE)
- ✅ **JWT Token Management** - Secure authentication
- ✅ **Teams & Channels** - Microsoft Teams-like navigation
- ✅ **Real-time Chat** - WebSocket-based messaging
- ✅ **Meetings** - Instant calls and scheduled meetings
- ✅ **File Management** - Upload, download, lock/unlock files
- ✅ **Task Management** - Jira-like task tracking
- ✅ **Notifications** - Real-time notification system
- ✅ **Responsive UI** - Clean, professional design with Tailwind CSS

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend API Gateway running on `http://localhost:8100`
- All microservices running

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:5173` (default Vite port).

### Build for Production

```bash
# Build
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── layouts/        # Layout components (Sidebar, TopBar, MainLayout)
│   └── ProtectedRoute.tsx
├── contexts/           # React contexts
│   └── AuthContext.tsx # Authentication context
├── config/             # Configuration files
│   └── api.ts          # API configuration and utilities
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── TeamsPage.tsx
│   ├── ChatPage.tsx
│   ├── MeetingsPage.tsx
│   ├── FilesPage.tsx
│   ├── TasksPage.tsx
│   └── NotificationsPage.tsx
├── services/           # API services
│   ├── api.ts          # API client (axios)
│   └── websocket.ts    # WebSocket manager
├── types/              # TypeScript types
│   └── api.ts          # API-related types
└── App.tsx             # Main app component with routing
```

## API Configuration

All API calls go through the API Gateway at `http://localhost:8100`.

### Endpoints

- **Auth**: `/auth/login`, `/auth/employee/login`
- **Users**: `/users/profile`, `/users`
- **Teams**: `/teams`, `/teams/my`
- **Channels**: `/channels/teams/{teamId}/channels`
- **Chat**: `/chat/send`, `/chat/channel/{channelId}`, `/chat/direct/{userId}`
- **Meetings**: `/meetings/instant`, `/meetings/schedule`
- **Files**: `/files/upload`, `/files/channel/{channelId}`
- **Tasks**: `/tasks`, `/tasks/channel/{channelId}`
- **Notifications**: `/notifications`, `/notifications/unread`

### WebSocket Endpoints

- **Chat**: `ws://localhost:8100/ws/chat`
- **Notifications**: `ws://localhost:8100/ws/notifications`

## Authentication Flow

1. User logs in via `/login` page
2. JWT token is stored in `localStorage`
3. Token is automatically attached to all API requests
4. Token is decoded to extract user role and information
5. Protected routes check authentication before rendering
6. On logout, token is removed and user is redirected to login

## Routing

### Public Routes
- `/login` - Login page

### Protected Routes (Requires Authentication)
- `/app` - Main application layout
- `/app/teams` - Teams list
- `/app/teams/:teamId/channels/:channelId` - Channel chat
- `/app/chat` - Direct messages
- `/app/chat/:userId` - Direct chat with user
- `/app/meetings` - Meetings page
- `/app/files` - Files page
- `/app/tasks` - Tasks page
- `/app/notifications` - Notifications page

## Role-Based Access

- **ADMIN**: Full access to all features including admin panel
- **MANAGER**: Access to teams, channels, meetings, files, tasks
- **EMPLOYEE**: Access to assigned teams and channels

## WebSocket Integration

### Chat WebSocket
- Connects when entering a channel or direct chat
- Subscribes to channel/user topic
- Receives real-time messages
- Automatically disconnects when leaving chat

### Notifications WebSocket
- Connects on app load
- Subscribes to user-specific notification topic
- Receives real-time notifications
- Updates notification count in top bar

## State Management

- **Auth Context**: Manages authentication state, user info, and JWT token
- **API Client**: Handles all HTTP requests with automatic JWT attachment
- **WebSocket Manager**: Manages WebSocket connections and subscriptions

## Environment Variables

Create a `.env` file (optional):

```env
VITE_API_BASE_URL=http://localhost:8100
VITE_WS_BASE_URL=ws://localhost:8100
```

## Development Notes

- All API calls are type-safe with TypeScript interfaces
- JWT token is automatically included in Authorization header
- WebSocket connections are automatically managed
- Error handling is implemented for all API calls
- Loading states are shown during async operations
- Responsive design works on desktop and mobile

## Troubleshooting

### Cannot connect to API Gateway
- Ensure API Gateway is running on port 8100
- Check CORS configuration in API Gateway
- Verify backend services are running

### JWT token expired
- Token expiration is checked automatically
- User is redirected to login on 401 error
- Token refresh logic can be added if needed

### WebSocket connection fails
- Check WebSocket endpoint in API Gateway
- Verify JWT token is valid
- Check browser console for connection errors

## Next Steps

- [ ] Add token refresh mechanism
- [ ] Implement file preview
- [ ] Add meeting video integration
- [ ] Implement search functionality
- [ ] Add user profile page
- [ ] Implement dark mode
- [ ] Add unit tests
- [ ] Add E2E tests
