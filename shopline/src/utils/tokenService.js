/**
 * tokenService.js
 *
 * Handles JWT storage using localStorage.
 *
 * SECURITY NOTE: Storing tokens in localStorage exposes them to XSS attacks
 * because any JavaScript running on the page (including injected scripts) can
 * read localStorage. The ideal alternative is an httpOnly cookie set by the
 * server, which is inaccessible to JavaScript. For this course demo we accept
 * the tradeoff and document it here so developers are aware.
 */

const TOKEN_KEY = 'shopline_auth_token'

export const tokenService = {
  /** Read the stored JWT, or null if absent. */
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  /** Persist a JWT to localStorage. */
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token)
  },

  /** Remove the JWT from localStorage (used on logout). */
  clearToken() {
    localStorage.removeItem(TOKEN_KEY)
  },
}
