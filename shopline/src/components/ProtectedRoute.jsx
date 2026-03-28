import { useSelector } from 'react-redux'
import { Navigate, useLocation } from 'react-router-dom'
import { selectToken } from '../store/authSlice'

/**
 * ProtectedRoute — wraps any route that requires authentication.
 * If there is no token in Redux state (which is seeded from localStorage on
 * app init), the user is redirected to /signin.  The current path is stored
 * in location state so the user can be sent back after logging in.
 */
export default function ProtectedRoute({ children }) {
  const token = useSelector(selectToken)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/signin" state={{ from: location }} replace />
  }

  return children
}
