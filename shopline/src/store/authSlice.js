import { createSlice } from '@reduxjs/toolkit'
import { tokenService } from '../utils/tokenService'

/**
 * Auth slice — manages authentication state.
 *
 * State shape:
 *   user    — { email, name } | null   (passwords are NEVER stored here)
 *   token   — JWT string | null
 *   loading — boolean
 *   error   — string | null
 */
const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    // Rehydrate token from localStorage on app init so the user stays logged
    // in across page refreshes.
    token: tokenService.getToken(),
    loading: false,
    error: null,
  },
  reducers: {
    /** Called on successful sign-in or sign-up. Persists token to storage. */
    setCredentials(state, action) {
      const { user, token } = action.payload
      state.user = user
      state.token = token
      state.error = null
      tokenService.setToken(token)
    },

    /** Sets loading flag while an auth request is in-flight. */
    setLoading(state, action) {
      state.loading = action.payload
    },

    /** Stores an error message to display in the UI. */
    setAuthError(state, action) {
      state.error = action.payload
      state.loading = false
    },

    /** Clears all auth state and removes the token from storage (logout). */
    logout(state) {
      state.user = null
      state.token = null
      state.loading = false
      state.error = null
      tokenService.clearToken()
    },
  },
})

export const { setCredentials, setLoading, setAuthError, logout } =
  authSlice.actions

export default authSlice.reducer

// Selectors
export const selectCurrentUser = (state) => state.auth.user
export const selectToken = (state) => state.auth.token
export const selectAuthLoading = (state) => state.auth.loading
export const selectAuthError = (state) => state.auth.error
