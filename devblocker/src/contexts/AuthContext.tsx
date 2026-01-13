/**
 * Authentication Context
 * Manages authentication state and JWT token
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import { decodeJWT, getToken, removeToken, isTokenExpired } from '../config/api';
import { authApi, userApi } from '../api';
import type { RegisterRequest, LoginRequest, JWTPayload, User, UserRole } from '../types/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  role: UserRole | null;
  isLoading: boolean;
  register: (data: RegisterRequest) => Promise<void>;
  login: (credentials: LoginRequest) => Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }>;
  employeeLogin: (credentials: LoginRequest) => Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }>;
  organizationLogin: (credentials: LoginRequest) => Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  getRedirectPath: (role: UserRole | null, isFirstLogin?: boolean, profileSetupRequired?: boolean) => string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Refresh user profile
   */
  const refreshUser = useCallback(async (): Promise<void> => {
    const storedToken = getToken();
    if (!storedToken) {
      return;
    }

    const decoded = decodeJWT(storedToken);
    if (!decoded) {
      return;
    }

    // Try to get user by ID from token (only if we have organizationId)
    if (decoded.userId && decoded.organizationId && decoded.organizationId > 0) {
      try {
        const userProfile = await userApi.getUserById(decoded.userId);
        setUser(userProfile);
        return;
      } catch (error) {
        // If getUserById fails (e.g., 403 if not ADMIN), fall through to token-based user
        console.warn('Could not fetch user profile, using token data:', error);
      }
    }

    // Don't try the /users/profile endpoint - it doesn't exist
    // Just construct user from token
    const basicUser: User = {
      id: decoded.userId,
      email: decoded.email,
      role: decoded.role,
      organizationId: decoded.organizationId || 0,
      status: 'ACTIVE' as const,
      createdAt: new Date().toISOString(),
    };
    setUser(basicUser);
  }, []);

  // Initialize auth state from localStorage
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = getToken();
      if (storedToken && !isTokenExpired(storedToken)) {
        setTokenState(storedToken);
        try {
          await refreshUser();
        } catch (error) {
          console.error('Error refreshing user:', error);
          removeToken();
          setTokenState(null);
          setUser(null);
        }
      } else {
        if (storedToken) {
          removeToken(); // Remove expired token
        }
        setTokenState(null);
        setUser(null);
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, [refreshUser]);

  /**
   * Register (Admin)
   */
  const register = useCallback(async (data: RegisterRequest): Promise<void> => {
    try {
      const response = await authApi.register(data);
      setTokenState(response.token);
      
      // After registration, create a user object from the response
      // We don't fetch profile since /users/profile doesn't exist and
      // user might not have organizationId yet
      const basicUser: User = {
        id: response.userId,
        email: response.email,
        firstName: data.firstName,
        lastName: data.lastName,
        role: response.role,
        organizationId: response.organizationId || 0,
        status: 'ACTIVE' as const,
        createdAt: new Date().toISOString(),
      };
      setUser(basicUser);
    } catch (error: any) {
      console.error('Registration error:', error);
      throw error;
    }
  }, []);

  /**
   * Login (Admin/Manager)
   */
  const login = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.login(credentials);
      setTokenState(response.token);
      await refreshUser();
      return {
        role: response.role,
        isFirstLogin: response.isFirstLogin,
        profileSetupRequired: response.profileSetupRequired,
      };
    } catch (error: any) {
      console.error('Login error:', error);
      throw error;
    }
  }, [refreshUser]);

  /**
   * Employee Login
   */
  const employeeLogin = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.employeeLogin(credentials);
      setTokenState(response.token);
      await refreshUser();
      return {
        role: response.role,
        isFirstLogin: response.isFirstLogin,
        profileSetupRequired: response.profileSetupRequired,
      };
    } catch (error: any) {
      console.error('Employee login error:', error);
      throw error;
    }
  }, [refreshUser]);

  /**
   * Organization Login
   */
  const organizationLogin = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.organizationLogin(credentials);
      setTokenState(response.token);
      await refreshUser();
      return {
        role: response.role,
        isFirstLogin: response.isFirstLogin,
        profileSetupRequired: response.profileSetupRequired,
      };
    } catch (error: any) {
      console.error('Organization login error:', error);
      throw error;
    }
  }, [refreshUser]);

  /**
   * Logout
   */
  const logout = useCallback((): void => {
    authApi.logout();
    setTokenState(null);
    setUser(null);
  }, []);

  /**
   * Get redirect path based on role and setup status
   */
  const getRedirectPath = useCallback((userRole: UserRole | null, isFirstLogin?: boolean, profileSetupRequired?: boolean): string => {
    if (!userRole) {
      return '/login';
    }

    // Handle first login / onboarding flows
    if (isFirstLogin || profileSetupRequired) {
      switch (userRole) {
        case 'ADMIN':
          return '/admin/onboarding';
        case 'MANAGER':
        case 'EMPLOYEE':
          return '/employee/profile-setup';
        case 'EXTERNAL_USER':
          return '/app/teams'; // External users go straight to teams
        default:
          return '/login';
      }
    }

    // Handle normal redirects based on role
    switch (userRole) {
      case 'ADMIN':
        return '/app/teams'; // Redirect to teams page instead of admin dashboard for now
      case 'MANAGER':
      case 'EMPLOYEE':
        return '/app/teams';
      case 'EXTERNAL_USER':
        return '/app/teams';
      default:
        return '/login';
    }
  }, []);

  // Decode role from token
  const decoded: JWTPayload | null = token ? decodeJWT(token) : null;
  const role = decoded?.role || user?.role || null;

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user && !isTokenExpired(token),
    role,
    isLoading,
    register,
    login,
    employeeLogin,
    organizationLogin,
    logout,
    refreshUser,
    getRedirectPath,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Hook to use auth context
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

