import { configureStore } from '@reduxjs/toolkit'
import authReducer from './authSlice'

/**
 * Redux store.
 *
 * Redux Toolkit's configureStore automatically enables the Redux DevTools
 * Extension when it is installed in the browser, with clear action names
 * (auth/setCredentials, auth/logout, etc.) visible in the DevTools panel.
 */
const store = configureStore({
  reducer: {
    auth: authReducer,
  },
})

export default store
