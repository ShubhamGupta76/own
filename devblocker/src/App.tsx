/**
 * Main App Component
 * Sets up routing and authentication with Microsoft Teams-like structure
 */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { TeamProvider } from './contexts/TeamContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { MainLayout } from './components/layouts/MainLayout';
import HomePage from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OrganizationRegistrationPage } from './pages/OrganizationRegistrationPage';
import { AdminOnboardingPage } from './pages/admin/AdminOnboardingPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminUsersPage } from './pages/admin/AdminUsersPage';
import { AdminTeamsPage } from './pages/admin/AdminTeamsPage';
import { AdminPoliciesPage } from './pages/admin/AdminPoliciesPage';
import { AdminSettingsPage } from './pages/admin/AdminSettingsPage';
import { EmployeeProfileSetupPage } from './pages/employee/EmployeeProfileSetupPage';
import { TeamsLandingPage } from './pages/TeamsLandingPage';
import { ChannelView } from './pages/ChannelView';
import { ChatPage } from './pages/ChatPage';
import { MeetingsPage } from './pages/MeetingsPage';
import { MeetingRoomPage } from './pages/MeetingRoomPage';
import { FilesPage } from './pages/FilesPage';
import { TasksPage } from './pages/TasksPage';
import { NotificationsPage } from './pages/NotificationsPage';

// Placeholder pages
const ActivityPage = () => <NotificationsPage />;
const SettingsPage = () => (
  <div className="p-6">
    <h1 className="text-2xl font-bold mb-4">Settings</h1>
    <p className="text-gray-600">Settings page coming soon...</p>
  </div>
);

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <TeamProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/register-org" element={<OrganizationRegistrationPage />} />
            
            {/* Admin Onboarding - First Login */}
            <Route
              path="/admin/onboarding"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminOnboardingPage />
                </ProtectedRoute>
              }
            />
            
            {/* Employee Profile Setup - First Login */}
            <Route
              path="/employee/profile-setup"
              element={
                <ProtectedRoute allowedRoles={['EMPLOYEE', 'MANAGER']}>
                  <EmployeeProfileSetupPage />
                </ProtectedRoute>
              }
            />
            
            {/* Meeting Room - Full Screen (outside MainLayout) */}
            <Route
              path="/app/meetings/:meetingId/room"
              element={
                <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER']}>
                  <MeetingRoomPage />
                </ProtectedRoute>
              }
            />

            {/* Admin Routes */}
            <Route
              path="/app/admin"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <MainLayout />
                </ProtectedRoute>
              }
            >
              <Route path="dashboard" element={<AdminDashboardPage />} />
              <Route path="users" element={<AdminUsersPage />} />
              <Route path="teams" element={<AdminTeamsPage />} />
              <Route path="policies" element={<AdminPoliciesPage />} />
              <Route path="settings" element={<AdminSettingsPage />} />
              <Route index element={<Navigate to="/app/admin/dashboard" replace />} />
            </Route>
            
            {/* Main App Routes - All authenticated users */}
            <Route
              path="/app"
              element={
                <ProtectedRoute>
                  <MainLayout />
                </ProtectedRoute>
              }
            >
              {/* Teams & Channels - Landing Page */}
              <Route 
                path="teams" 
                element={<TeamsLandingPage />} 
              />
              
              {/* Channel View with Tabs - Restricted for EXTERNAL_USER */}
              <Route 
                path="teams/:teamId/channels/:channelId" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER']}>
                    <ChannelView />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="teams/:teamId/channels/:channelId/:tab" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER']}>
                    <ChannelView />
                  </ProtectedRoute>
                } 
              />
              
              {/* Direct Chat - Not available for EXTERNAL_USER */}
              <Route 
                path="chat" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']}>
                    <div className="flex items-center justify-center h-full text-gray-500">
                      Select a user to start chatting
                    </div>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="chat/:userId" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']}>
                    <ChatPage roomType="direct" />
                  </ProtectedRoute>
                } 
              />
              
              {/* Standalone Pages - All authenticated users */}
              <Route 
                path="meetings" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER']}>
                    <MeetingsPage />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="files" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE', 'EXTERNAL_USER']}>
                    <FilesPage />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="tasks" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']}>
                    <TasksPage />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="activity" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']}>
                    <ActivityPage />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="settings" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'EMPLOYEE']}>
                    <SettingsPage />
                  </ProtectedRoute>
                } 
              />
              
              {/* Default redirect - role-based */}
              <Route index element={<Navigate to="/app/teams" replace />} />
            </Route>
            
            {/* Catch all - redirect to home */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </TeamProvider>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
