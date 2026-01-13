import { useState, useEffect } from 'react'
import { userApi } from '../api'
import type { User } from '../types'

// Mock data for testing UI
const mockUser: User = {
  id: '1',
  name: 'Shubham Gupta',
  email: 'shubham@example.com',
  status: 'online',
  avatar: undefined
}

// Convert API User to app User type
const mapApiUserToAppUser = (apiUser: any): User => ({
  id: String(apiUser.id),
  name: apiUser.displayName || `${apiUser.firstName || ''} ${apiUser.lastName || ''}`.trim() || apiUser.email,
  email: apiUser.email,
  status: 'online',
  avatar: undefined
})

export function useCurrentUser() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    const fetchUser = async () => {
      try {
        setLoading(true)
        // Try API first, fallback to mock data
        try {
          const data = await userApi.getCurrentUser()
          setUser(mapApiUserToAppUser(data))
        } catch {
          // Use mock data when backend is not available
          setUser(mockUser)
        }
        setError(null)
      } catch (err) {
        setError(err as Error)
        setUser(mockUser) // Fallback to mock data
      } finally {
        setLoading(false)
      }
    }

    fetchUser()
  }, [])

  return { user, loading, error, refetch: () => userApi.getCurrentUser().then(mapApiUserToAppUser).then(setUser).catch(() => setUser(mockUser)) }
}

