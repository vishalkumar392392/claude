---
name: Shopline Authentication UX Design
description: UX spec for Sign In and Sign Up flows in the Shopline SPA, including JWT handling, Redux auth slice, protected routes, and token refresh.
type: project
---

The Shopline project is a React + Vite SPA using Tailwind CSS, Redux with DevTools, and JWT-based authentication.

A full UX spec was produced for the auth feature including:
- Sign In page (`/signin`) and Sign Up page (`/signup`) with centered AuthCard layout
- Redux `authSlice` shape: `{ user, accessToken, isAuthenticated, isLoading, error }`
- Access token stored in Redux memory only (never localStorage)
- Refresh token stored in httpOnly cookie (preferred) or sessionStorage (fallback)
- Axios interceptor for attaching Bearer token and handling 401 → silent refresh
- ProtectedRoute component redirects unauthenticated users with `location.state.from` preserved
- AuthInitializer component handles token rehydration on app boot (shows full-page spinner)
- Auto-login after registration (no email verification step assumed)
- Session expiry message passed via React Router `state` to Sign In page

**Why:** Shopline is a course project demonstrating multi-agent orchestration; auth is the foundational security layer.
**How to apply:** Use this context when designing or implementing any feature that touches auth state, user identity, or protected routes.
