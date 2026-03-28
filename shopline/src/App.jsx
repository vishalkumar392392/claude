import { useState } from 'react'
import { BrowserRouter, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'
import SignIn from './pages/SignIn'
import SignUp from './pages/SignUp'
import ProtectedRoute from './components/ProtectedRoute'
import { logout, selectCurrentUser, selectToken } from './store/authSlice'

// ── Header ───────────────────────────────────────────────────────────────────

function Header() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const token = useSelector(selectToken)
  const user = useSelector(selectCurrentUser)

  function handleLogout() {
    dispatch(logout())
    navigate('/signin', { replace: true })
  }

  return (
    <header className="w-full flex items-center justify-between px-6 py-3 border-b border-(--border) bg-(--bg)">
      <Link
        to="/"
        className="text-lg font-semibold tracking-tight text-(--text-h) hover:text-(--accent) transition-colors"
      >
        Shopline
      </Link>

      <nav className="flex items-center gap-3">
        {token ? (
          <>
            {user && (
              <span className="text-sm text-(--text) hidden sm:inline">
                {user.name}
              </span>
            )}
            <button
              onClick={handleLogout}
              className="rounded-lg border border-(--border) bg-transparent px-4 py-1.5 text-sm font-medium text-(--text-h) hover:border-(--accent) hover:text-(--accent) transition"
            >
              Sign out
            </button>
          </>
        ) : (
          <>
            <Link
              to="/signin"
              className="rounded-lg border border-(--border) bg-transparent px-4 py-1.5 text-sm font-medium text-(--text-h) hover:border-(--accent) hover:text-(--accent) transition"
            >
              Sign in
            </Link>
            <Link
              to="/signup"
              className="rounded-lg bg-(--accent) hover:opacity-90 px-4 py-1.5 text-sm font-medium text-white transition"
            >
              Sign up
            </Link>
          </>
        )}
      </nav>
    </header>
  )
}

// ── Toast display on redirect from SignUp ────────────────────────────────────

function SuccessToast() {
  const location = useLocation()
  const toast = location.state?.toast
  const [visible, setVisible] = useState(!!toast)

  if (!visible || !toast) return null

  return (
    <div
      role="status"
      className="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 rounded-xl bg-green-600 text-white text-sm font-medium px-5 py-3 shadow-lg"
    >
      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
      </svg>
      {toast}
      <button
        onClick={() => setVisible(false)}
        className="ml-2 text-white/70 hover:text-white focus:outline-none"
        aria-label="Dismiss"
      >
        ×
      </button>
    </div>
  )
}

// ── Home page (original Vite starter content) ────────────────────────────────

function Home() {
  const [count, setCount] = useState(0)

  return (
    <>
      <section id="center">
        <div className="hero">
          <img src={heroImg} className="base" width="170" height="179" alt="" />
          <img src={reactLogo} className="framework" alt="React logo" />
          <img src={viteLogo} className="vite" alt="Vite logo" />
        </div>
        <div>
          <h1>Get started</h1>
          <p>
            Edit <code>src/App.jsx</code> and save to test <code>HMR</code>
          </p>
        </div>
        <button
          className="counter"
          onClick={() => setCount((count) => count + 1)}
        >
          Count is {count}
        </button>
      </section>

      <div className="ticks"></div>

      <section id="next-steps">
        <div id="docs">
          <svg className="icon" role="presentation" aria-hidden="true">
            <use href="/icons.svg#documentation-icon"></use>
          </svg>
          <h2>Documentation</h2>
          <p>Your questions, answered</p>
          <ul>
            <li>
              <a href="https://vite.dev/" target="_blank">
                <img className="logo" src={viteLogo} alt="" />
                Explore Vite
              </a>
            </li>
            <li>
              <a href="https://react.dev/" target="_blank">
                <img className="button-icon" src={reactLogo} alt="" />
                Learn more
              </a>
            </li>
          </ul>
        </div>
        <div id="social">
          <svg className="icon" role="presentation" aria-hidden="true">
            <use href="/icons.svg#social-icon"></use>
          </svg>
          <h2>Connect with us</h2>
          <p>Join the Vite community</p>
          <ul>
            <li>
              <a href="https://github.com/vitejs/vite" target="_blank">
                <svg className="button-icon" role="presentation" aria-hidden="true">
                  <use href="/icons.svg#github-icon"></use>
                </svg>
                GitHub
              </a>
            </li>
            <li>
              <a href="https://chat.vite.dev/" target="_blank">
                <svg className="button-icon" role="presentation" aria-hidden="true">
                  <use href="/icons.svg#discord-icon"></use>
                </svg>
                Discord
              </a>
            </li>
            <li>
              <a href="https://x.com/vite_js" target="_blank">
                <svg className="button-icon" role="presentation" aria-hidden="true">
                  <use href="/icons.svg#x-icon"></use>
                </svg>
                X.com
              </a>
            </li>
            <li>
              <a href="https://bsky.app/profile/vite.dev" target="_blank">
                <svg className="button-icon" role="presentation" aria-hidden="true">
                  <use href="/icons.svg#bluesky-icon"></use>
                </svg>
                Bluesky
              </a>
            </li>
          </ul>
        </div>
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

// ── App root with router ──────────────────────────────────────────────────────

function AppShell() {
  return (
    <>
      <SuccessToast />
      <Header />
      <Routes>
        {/* Public routes */}
        <Route path="/signin" element={<SignIn />} />
        <Route path="/signup" element={<SignUp />} />

        {/* Protected home route */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Home />
            </ProtectedRoute>
          }
        />

        {/* Catch-all → home (will redirect to /signin if unauthenticated) */}
        <Route
          path="*"
          element={
            <ProtectedRoute>
              <Home />
            </ProtectedRoute>
          }
        />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  )
}
