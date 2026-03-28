import { useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  setCredentials,
  setLoading,
  setAuthError,
  selectAuthLoading,
  selectAuthError,
} from '../store/authSlice'
import AuthLayout from '../components/AuthLayout'

const EyeIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
  </svg>
)

const EyeOffIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
  </svg>
)

const inputClass =
  'w-full rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition'

const inputErrorClass =
  'w-full rounded-lg border border-red-400 dark:border-red-500 bg-white dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-400 focus:border-transparent transition'

/**
 * SignIn page — /signin
 *
 * Uses react-hook-form with Controller for fully controlled inputs.
 * Replace the mock inside onSubmit with a real API call.
 */
export default function SignIn() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const loading = useSelector(selectAuthLoading)
  const apiError = useSelector(selectAuthError)

  const [showPassword, setShowPassword] = useState(false)

  const from = location.state?.from?.pathname || '/'

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: { email: '', password: '', rememberMe: false },
  })

  async function onSubmit({ email, password }) {
    dispatch(setLoading(true))
    dispatch(setAuthError(null))

    try {
      // ----------------------------------------------------------------
      // MOCK AUTH — replace with your real API endpoint.
      // e.g. const res = await fetch('/api/auth/signin', { method: 'POST',
      //        headers: { 'Content-Type': 'application/json' },
      //        body: JSON.stringify({ email, password }) })
      // ----------------------------------------------------------------
      await new Promise((r) => setTimeout(r, 800))

      if (email === 'demo@shopline.com' && password === 'password') {
        const mockToken = 'mock.jwt.token.' + Date.now()
        dispatch(setCredentials({ user: { email, name: 'Demo User' }, token: mockToken }))
        navigate(from, { replace: true })
      } else {
        dispatch(setAuthError('Invalid email or password. Please try again.'))
      }
    } catch {
      dispatch(setAuthError('Something went wrong. Please try again.'))
    } finally {
      dispatch(setLoading(false))
    }
  }

  return (
    <AuthLayout>
      <h2 className="text-center text-xl font-medium text-gray-700 dark:text-gray-300">
        Sign in to your account
      </h2>

      {/* API error banner */}
      {apiError && (
        <div
          role="alert"
          className="rounded-lg bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 px-4 py-3 text-sm text-red-700 dark:text-red-300"
        >
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
        {/* Email — controlled via Controller */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Email address
          </label>
          <Controller
            name="email"
            control={control}
            rules={{
              required: 'Email is required.',
              pattern: { value: /\S+@\S+\.\S+/, message: 'Enter a valid email.' },
            }}
            render={({ field }) => (
              <input
                {...field}
                id="email"
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                className={errors.email ? inputErrorClass : inputClass}
              />
            )}
          />
          {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
        </div>

        {/* Password — controlled via Controller */}
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Password
          </label>
          <div className="relative">
            <Controller
              name="password"
              control={control}
              rules={{ required: 'Password is required.' }}
              render={({ field }) => (
                <input
                  {...field}
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="••••••••"
                  className={`${errors.password ? inputErrorClass : inputClass} pr-10`}
                />
              )}
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 focus:outline-none"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            >
              {showPassword ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          </div>
          {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
        </div>

        {/* Remember me — controlled checkbox */}
        <div className="flex items-center gap-2">
          <Controller
            name="rememberMe"
            control={control}
            render={({ field: { value, onChange, ...rest } }) => (
              <input
                {...rest}
                id="remember-me"
                type="checkbox"
                checked={value}
                onChange={(e) => onChange(e.target.checked)}
                className="h-4 w-4 rounded border-gray-300 text-violet-600 focus:ring-violet-500"
              />
            )}
          />
          <label htmlFor="remember-me" className="text-sm text-gray-600 dark:text-gray-400 select-none">
            Remember me
          </label>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={loading}
          className="w-full flex justify-center items-center gap-2 rounded-lg bg-violet-600 hover:bg-violet-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-medium py-2.5 text-sm transition focus:outline-none focus:ring-2 focus:ring-violet-500 focus:ring-offset-2"
        >
          {loading && (
            <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" aria-hidden="true">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
            </svg>
          )}
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="text-center text-sm text-gray-500 dark:text-gray-400">
        Don&apos;t have an account?{' '}
        <Link to="/signup" className="font-medium text-violet-600 hover:text-violet-500 dark:text-violet-400">
          Sign up
        </Link>
      </p>
    </AuthLayout>
  )
}
