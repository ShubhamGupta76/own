import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import { decodeJWT, getToken, setToken, removeToken, isTokenExpired } from '../config/api';
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
  setToken: (token: string) => void;
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

  const refreshUser = useCallback(async (): Promise<void> => {
    const storedToken = getToken();
    if (!storedToken) {
      return;
    }

    const decoded = decodeJWT(storedToken);
    if (!decoded) {
      return;
    }

    // Extract organizationId from JWT (can be number or null)
    const orgId = decoded.organizationId != null ? 
      (typeof decoded.organizationId === 'number' ? decoded.organizationId : Number(decoded.organizationId)) : 
      null;

    // For ADMIN role, try to fetch full profile from User Service
    if (decoded.role === 'ADMIN' && decoded.userId && orgId && orgId > 0) {
      try {
        const userProfile = await userApi.getUserById(decoded.userId);
        setUser(userProfile);
        return;
      } catch (error: any) {
        const status = error.response?.status;
        const errorMessage = error.response?.data?.message || error.message || '';
        
        if (status === 404 || 
            status === 400 && (errorMessage.toLowerCase().includes('not found') || 
                               errorMessage.toLowerCase().includes('does not exist'))) {
          console.debug('User not found in User service, using token data');
        } else if (status === 403) {
          console.debug('Access denied to user profile, using token data');
        } else {
          console.warn('Could not fetch user profile, using token data:', status || errorMessage);
        }
      }
    }
    
    // For all roles, create user object from JWT claims
    const basicUser: User = {
      id: decoded.userId,
      email: decoded.email,
      role: decoded.role,
      organizationId: orgId || 0, // Use extracted orgId or default to 0
      status: 'ACTIVE' as const,
      createdAt: new Date().toISOString(),
    };
    setUser(basicUser);
  }, []);

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
            removeToken();
          }
        setTokenState(null);
        setUser(null);
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, [refreshUser]);

  const register = useCallback(async (data: RegisterRequest): Promise<void> => {
    try {
      const response = await authApi.register(data);
      setTokenState(response.token);
      
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

  const login = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.login(credentials);
      
      if (response.token) {
        setTokenState(response.token);
        setToken(response.token);
        
        // Small delay to ensure token is stored before making requests
        await new Promise(resolve => setTimeout(resolve, 50));
        
        await refreshUser();
        
        return {
          role: response.role,
          isFirstLogin: response.isFirstLogin,
          profileSetupRequired: response.profileSetupRequired,
        };
      } else {
        throw new Error('No token received from login');
      }
    } catch (error: any) {
      console.error('Login error:', error);
      throw error;
    }
  }, [refreshUser]);

  const employeeLogin = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.employeeLogin(credentials);
      
      if (response.token) {
        setTokenState(response.token);
        setToken(response.token);
        
        // Small delay to ensure token is stored before making requests
        await new Promise(resolve => setTimeout(resolve, 50));
        
        await refreshUser();
        
        return {
          role: response.role,
          isFirstLogin: response.isFirstLogin,
          profileSetupRequired: response.profileSetupRequired,
        };
      } else {
        throw new Error('No token received from login');
      }
    } catch (error: any) {
      console.error('Employee login error:', error);
      throw error;
    }
  }, [refreshUser]);

  const organizationLogin = useCallback(async (credentials: LoginRequest): Promise<{ role: UserRole; isFirstLogin?: boolean; profileSetupRequired?: boolean }> => {
    try {
      const response = await authApi.organizationLogin(credentials);
      
      if (response.token) {
        setTokenState(response.token);
        setToken(response.token);
        
        // Small delay to ensure token is stored before making requests
        await new Promise(resolve => setTimeout(resolve, 50));
        
        await refreshUser();
        
        return {
          role: response.role,
          isFirstLogin: response.isFirstLogin,
          profileSetupRequired: response.profileSetupRequired,
        };
      } else {
        throw new Error('No token received from login');
      }
    } catch (error: any) {
      console.error('Organization login error:', error);
      throw error;
    }
  }, [refreshUser]);

  const logout = useCallback((): void => {
    authApi.logout();
    setTokenState(null);
    setUser(null);
  }, []);

  const getRedirectPath = useCallback((userRole: UserRole | null, isFirstLogin?: boolean, profileSetupRequired?: boolean): string => {
    if (!userRole) {
      return '/login';
    }

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

    switch (userRole) {
      case 'ADMIN':
        return '/app/teams';
      case 'MANAGER':
      case 'EMPLOYEE':
        return '/app/teams';
      case 'EXTERNAL_USER':
        return '/app/teams';
      default:
        return '/login';
    }
  }, []);

  const decoded: JWTPayload | null = token ? decodeJWT(token) : null;
  const role = decoded?.role || user?.role || null;

  const updateToken = useCallback((newToken: string) => {
    setToken(newToken);
    setTokenState(newToken);
  }, []);

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
    setToken: updateToken,
    getRedirectPath,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

