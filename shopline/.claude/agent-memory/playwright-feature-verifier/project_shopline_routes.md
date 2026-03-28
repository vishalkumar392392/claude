---
name: Shopline App Routes and Auth Patterns
description: Known routes, auth flow, mock credentials, and UI patterns for the Shopline React app
type: project
---

Application runs on port 5173 via `npm run dev` (Vite). The verifier config says 5000 but the actual dev port is 5173.

**Why:** Vite defaults to 5173; the agent-memory default of 5000 is stale for this project.

**How to apply:** Always target http://127.0.0.1:5173 when running verification against the Shopline dev server. Start with `npm run dev -- --host 127.0.0.1` from the project root if not already running.

## Known Routes
- `/` — Home page (protected; redirects to `/signin` when logged out)
- `/signin` — Sign-in form (email + password, password visibility toggle, "Remember me" checkbox, error banner on bad credentials, link to /signup)
- `/signup` — Sign-up form (Full name, Email, Password with visibility toggle, Confirm password with visibility toggle, "Create account" button, link to /signin)

## Auth Patterns
- Mock credentials: email `demo@shopline.com` / password `password`
- After login, header shows "Demo User" + "Sign out" button
- When logged out, header shows "Sign in" (outlined) + "Sign up" (filled purple) buttons
- JWT stored in localStorage via tokenService.js; Redux slice has `setCredentials` / `logout`
- ProtectedRoute redirects unauthenticated users to /signin — so navigating to / while logged out lands on /signin

## UI Verification Notes
- The home page (/) is a ProtectedRoute; it redirects to /signin when not authenticated, so step-1 screenshot of "home with Sign In/Sign Up" will actually show the /signin page
- Error banner on bad login appears above the email field, styled with red text on pink background: "Invalid email or password. Please try again."
- The `playwright` package must be installed locally (`npm install playwright`) and Chromium downloaded (`npx playwright install chromium`) — it is NOT in the project's package.json
- Project uses `"type": "module"` in package.json, so Playwright scripts must use `.cjs` extension or ESM `import` syntax
