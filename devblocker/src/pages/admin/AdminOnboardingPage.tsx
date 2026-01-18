/**
 * Admin Onboarding Page
 * First-time setup for admin users
 * Route: /admin/onboarding
 */

import React, { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { decodeJWT, getToken } from '../../config/api';
import { organizationApi } from '../../api/organization.api';

export const AdminOnboardingPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, refreshUser, setToken } = useAuth();
  
  const [step, setStep] = useState(1);
  const [organizationName, setOrganizationName] = useState('');
  const [organizationDescription, setOrganizationDescription] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [adminEmail, setAdminEmail] = useState<string>('');

  // Check if organization already exists and redirect if it does
  useEffect(() => {
    const checkExistingOrganization = async () => {
      try {
        // First check if token has organizationId
        const token = getToken();
        if (token) {
          const decoded = decodeJWT(token);
          if (decoded?.organizationId && decoded.organizationId !== 0) {
            // Token already has organizationId, check if org exists
            try {
              const org = await organizationApi.getMyOrganization();
              if (org && org.id) {
                // Organization exists and token is valid, go to dashboard
                navigate('/app/admin/dashboard', { replace: true });
                return;
              }
            } catch (orgErr: any) {
              // If getMyOrganization fails but we have organizationId in token,
              // try refreshing user and redirect
              if (orgErr.response?.status === 404) {
                // Organization might not exist yet, continue with onboarding
                return;
              }
            }
          }
        }

        // Try to get organization
        const org = await organizationApi.getMyOrganization();
        if (org && org.id) {
          // Organization exists, refresh user and redirect
          await refreshUser();
          navigate('/app/admin/dashboard', { replace: true });
        }
      } catch (err: any) {
        // Organization doesn't exist yet (404) - this is expected during onboarding
        // Only log unexpected errors
        const status = err.response?.status;
        if (status && status !== 404 && status !== 403) {
          console.error('Error checking organization:', err);
        }
        // Silently continue with onboarding for 404 (org doesn't exist) and 403 (not authorized yet)
      }
    };

    checkExistingOrganization();
  }, [navigate, refreshUser]);

  // Get admin email from JWT token or user context
  useEffect(() => {
    const token = getToken();
    if (token) {
      const decoded = decodeJWT(token);
      if (decoded?.email) {
        setAdminEmail(decoded.email);
      } else if (user?.email) {
        setAdminEmail(user.email);
      } else {
        // Fallback to localStorage
        const storedEmail = localStorage.getItem('user_email');
        if (storedEmail) {
          setAdminEmail(storedEmail);
        }
      }
    } else if (user?.email) {
      setAdminEmail(user.email);
    }
  }, [user]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      // First check if organization already exists
      try {
        const existingOrg = await organizationApi.getMyOrganization();
        if (existingOrg && existingOrg.id) {
          // Organization already exists, just refresh and redirect
          await refreshUser();
          navigate('/app/admin/dashboard', { replace: true });
          return;
        }
      } catch (checkErr: any) {
        // Organization doesn't exist (404), continue with creation
        if (checkErr.response?.status !== 404) {
          // Unexpected error, log it but continue
          console.warn('Error checking existing organization:', checkErr);
        }
      }

      // Create organization via API
      const response = await organizationApi.createOrganization({
        name: organizationName,
        domain: undefined, // Optional domain can be added later
      });

      // Token is automatically updated by organizationApi.createOrganization in localStorage
      // Update token state in AuthContext and refresh user data
      if (response.token) {
        setToken(response.token);
        // Small delay to ensure token is stored
        await new Promise(resolve => setTimeout(resolve, 100));
        // Refresh user context to get updated organizationId
        await refreshUser();
      }
      
      navigate('/app/admin/dashboard', { replace: true });
    } catch (err: any) {
      let errorMessage = 'Failed to create organization. Please try again.';
      
      if (err.response?.data) {
        errorMessage = err.response.data.message || 
                      err.response.data.error || 
                      err.message || 
                      errorMessage;
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      // Provide more helpful error messages based on backend response
      const errorData = err.response?.data;
      const errorCode = errorData?.errorCode;
      
      if (err.response?.status === 500) {
        // Check if it's a service communication error
        if (errorCode === 'SERVICE_UNAVAILABLE' || 
            errorMessage.toLowerCase().includes('auth service') ||
            errorMessage.toLowerCase().includes('cannot connect') ||
            errorMessage.toLowerCase().includes('not running')) {
          errorMessage = 'Auth service is not available. Please ensure the Auth service is running and try again.';
        } else if (errorMessage.toLowerCase().includes('already exists')) {
          // Organization already exists, try to get it and redirect
          errorMessage = 'Organization already exists. Redirecting...';
          try {
            await refreshUser();
            navigate('/app/admin/dashboard', { replace: true });
            return;
          } catch (refreshErr) {
            errorMessage = 'Organization already exists, but there was an error refreshing your session. Please try logging in again.';
          }
        } else {
          // Use backend message if available, otherwise generic message
          errorMessage = errorMessage || 'Server error while creating organization. Please check that all services are running and try again.';
        }
      } else if (err.response?.status === 403) {
        errorMessage = 'Access denied. Please ensure you are logged in as an admin.';
      } else if (err.response?.status === 400) {
        errorMessage = errorMessage || 'Invalid organization data. Please check your input and try again.';
      } else if (err.response?.status === 409) {
        // Organization already exists
        errorMessage = 'Organization already exists. Redirecting...';
        try {
          await refreshUser();
          navigate('/app/admin/dashboard', { replace: true });
          return;
        } catch (refreshErr) {
          errorMessage = 'Organization already exists, but there was an error refreshing your session. Please try logging in again.';
        }
      }
      
      setError(errorMessage);
      console.error('Onboarding error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4">
      <div className="max-w-2xl w-full bg-white rounded-xl shadow-lg p-8">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-lg flex items-center justify-center mx-auto mb-4">
            <span className="text-white font-bold text-2xl">DB</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Welcome to DevBlocker</h1>
          <p className="text-gray-600">Let's set up your organization</p>
        </div>

        {/* Progress Steps */}
        <div className="mb-8">
          <div className="flex items-center justify-center space-x-4">
            <div className={`flex items-center ${step >= 1 ? 'text-blue-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center border-2 ${step >= 1 ? 'border-blue-600 bg-blue-50' : 'border-gray-300'}`}>
                1
              </div>
              <span className="ml-2 text-sm font-medium">Organization</span>
            </div>
            <div className={`w-16 h-0.5 ${step >= 2 ? 'bg-blue-600' : 'bg-gray-300'}`} />
            <div className={`flex items-center ${step >= 2 ? 'text-blue-600' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center border-2 ${step >= 2 ? 'border-blue-600 bg-blue-50' : 'border-gray-300'}`}>
                2
              </div>
              <span className="ml-2 text-sm font-medium">Complete</span>
            </div>
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {step === 1 && (
            <div className="space-y-4">
              <div>
                <label htmlFor="organizationName" className="block text-sm font-medium text-gray-700 mb-2">
                  Organization Name *
                </label>
                <input
                  type="text"
                  id="organizationName"
                  value={organizationName}
                  onChange={(e) => setOrganizationName(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter organization name"
                  required
                />
              </div>

              <div>
                <label htmlFor="organizationDescription" className="block text-sm font-medium text-gray-700 mb-2">
                  Description (optional)
                </label>
                <textarea
                  id="organizationDescription"
                  value={organizationDescription}
                  onChange={(e) => setOrganizationDescription(e.target.value)}
                  rows={4}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Brief description of your organization"
                />
              </div>

              <button
                type="button"
                onClick={() => setStep(2)}
                disabled={!organizationName.trim()}
                className="w-full px-6 py-3 text-lg font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Continue
              </button>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <div className="text-center py-8">
                <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h2 className="text-2xl font-bold text-gray-900 mb-2">Review Your Information</h2>
                <p className="text-gray-600 mb-6">Please review and confirm your organization details</p>

                <div className="bg-gray-50 rounded-lg p-6 text-left space-y-3">
                  <div>
                    <span className="text-sm font-medium text-gray-500">Organization Name</span>
                    <p className="text-lg font-semibold text-gray-900">{organizationName}</p>
                  </div>
                  {organizationDescription && (
                    <div>
                      <span className="text-sm font-medium text-gray-500">Description</span>
                      <p className="text-gray-900">{organizationDescription}</p>
                    </div>
                  )}
                  <div>
                    <span className="text-sm font-medium text-gray-500">Admin Email</span>
                    <p className="text-gray-900">{adminEmail || user?.email || 'Loading...'}</p>
                  </div>
                </div>
              </div>

              <div className="flex gap-4">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="flex-1 px-6 py-3 text-lg font-semibold text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  Back
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="flex-1 px-6 py-3 text-lg font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isLoading ? 'Completing Setup...' : 'Complete Setup'}
                </button>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

