/**
 * Login Page
 * Handles Admin, Manager, and Employee login
 */

import React, { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, employeeLogin, organizationLogin, getRedirectPath } = useAuth();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [userType, setUserType] = useState<'admin' | 'employee' | 'organization'>('employee');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      let loginResponse;
      if (userType === 'admin') {
        loginResponse = await login({ email, password });
      } else if (userType === 'organization') {
        loginResponse = await organizationLogin({ email, password });
      } else {
        loginResponse = await employeeLogin({ email, password });
      }
      
      // Check if we're coming from a meeting join
      const fromState = location.state as any;
      if (fromState?.from?.meetingId) {
        // Redirect to meeting room
        navigate(`/app/meetings/${fromState.from.meetingId}/room`, { replace: true });
        return;
      }
      
      // Get redirect path based on role and setup status
      const redirectPath = getRedirectPath(
        loginResponse.role,
        loginResponse.isFirstLogin,
        loginResponse.profileSetupRequired
      );
      
      // Redirect based on role and setup status
      navigate(redirectPath, { replace: true });
    } catch (err: any) {
      // Backend returns error in 'message' field for AuthResponse
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message || 
                          'Login failed. Please check your credentials.';
      
      // Provide helpful hints based on error
      if (errorMessage.includes('Password not set')) {
        setError('Password not set for this account. Please contact your administrator to set your password.');
      } else if (errorMessage.includes('not assigned to an organization')) {
        setError('Your account is not assigned to an organization. Please contact your administrator.');
      } else if (errorMessage.includes('Account is disabled')) {
        setError('Your account is disabled. Please contact your administrator.');
      } else if (errorMessage.includes('Invalid') || errorMessage.includes('not found') || errorMessage.includes('credentials')) {
        const userTypeLabel = 
          userType === 'admin' ? 'Admin/Manager' : 
          userType === 'organization' ? 'Organization' : 
          'Employee';
        setError(`Invalid credentials. Please check your email and password. Make sure you selected "${userTypeLabel}" if you registered as ${userTypeLabel}. If you just registered, your administrator may need to set your password.`);
      } else {
        setError(errorMessage);
      }
      console.error('Login error:', err.response?.data || err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
        {/* Logo/Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">DevBlocker</h1>
          <p className="text-gray-600">Teams & Jira Collaboration Platform</p>
        </div>

        {/* User Type Toggle */}
        <div className="mb-6 flex bg-gray-100 rounded-lg p-1">
          <button
            type="button"
            onClick={() => setUserType('admin')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              userType === 'admin'
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Admin/Manager
          </button>
          <button
            type="button"
            onClick={() => setUserType('employee')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              userType === 'employee'
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Employee
          </button>
          <button
            type="button"
            onClick={() => setUserType('organization')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              userType === 'organization'
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Organization
          </button>
        </div>

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
              placeholder="user@company.com"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-colors"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? 'Logging in...' : 'Sign In'}
          </button>
        </form>

        {/* Footer */}
        <div className="mt-6 text-center text-sm text-gray-500 space-y-2">
          <p>
            Don't have an organization?{' '}
            <Link 
              to="/register-org" 
              className="text-blue-600 hover:text-blue-700 font-medium"
            >
              Create organization
            </Link>
          </p>
          <p>
            Need to register as an employee? Contact your organization administrator.
          </p>
        </div>
      </div>
    </div>
  );
};

