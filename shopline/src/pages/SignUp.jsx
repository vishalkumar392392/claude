import { useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
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

const inputBase =
  'w-full rounded-lg border bg-white dark:bg-gray-700 px-3 py-2 text-sm text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:border-transparent transition'
const inputOk = `${inputBase} border-gray-300 dark:border-gray-600 focus:ring-violet-500`
const inputErr = `${inputBase} border-red-400 dark:border-red-500 focus:ring-red-400`

/** Returns { score: 0–3, label, barColor, textColor } */
function evaluatePasswordStrength(pwd) {
  let score = 0
  if (pwd.length >= 8) score++
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score++
  if (/\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) score++
  const labels    = ['', 'Weak',      'Medium',        'Strong']
  const colors    = ['', 'bg-red-400','bg-yellow-400', 'bg-green-500']
  const textColors= ['', 'text-red-500','text-yellow-500','text-green-600']
  return { score, label: labels[score], barColor: colors[score], textColor: textColors[score] }
}

/**
 * SignUp page — /signup
 *
 * Uses react-hook-form with Controller for fully controlled inputs.
 * confirmPassword is validated client-side only and never sent to the server.
 * Replace the mock inside onSubmit with a real API call.
 */
export default function SignUp() {
  const dispatch = useDispatch()
  const navigate = useNavigate()

  const loading = useSelector(selectAuthLoading)
  const apiError = useSelector(selectAuthError)

  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm]   = useState(false)

  const {
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: { name: '', email: '', password: '', confirmPassword: '' },
  })

  const passwordValue = watch('password', '')
  const strength = evaluatePasswordStrength(passwordValue)

  async function onSubmit(data) {
    const { email } = data
    dispatch(setAuthError(null))
    dispatch(setLoading(true))

    try {
      // ----------------------------------------------------------------
      // MOCK SIGN-UP — replace with your real API endpoint.
      // confirmPassword is intentionally NOT included — validated client-side only.
      // ----------------------------------------------------------------
      await new Promise((r) => setTimeout(r, 900))

      if (email === 'taken@shopline.com') {
        dispatch(setAuthError('An account with this email already exists.'))
        return
      }

      // On success redirect to /signin with a toast-style note
      navigate('/signin', {
        state: { toast: 'Account created! Please sign in.' },
        replace: true,
      })
    } catch {
      dispatch(setAuthError('Something went wrong. Please try again.'))
    } finally {
      dispatch(setLoading(false))
    }
  }

  return (
    <AuthLayout>
      <h2 className="text-center text-xl font-medium text-gray-700 dark:text-gray-300">
        Create your account
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
        {/* Full name — controlled via Controller */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Full name
          </label>
          <Controller
            name="name"
            control={control}
            rules={{ required: 'Full name is required.' }}
            render={({ field }) => (
              <input
                {...field}
                id="name"
                type="text"
                autoComplete="name"
                placeholder="Jane Doe"
                className={errors.name ? inputErr : inputOk}
              />
            )}
          />
          {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
        </div>

        {/* Email — controlled via Controller */}
        <div>
          <label htmlFor="signup-email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
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
                id="signup-email"
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                className={errors.email ? inputErr : inputOk}
              />
            )}
          />
          {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
        </div>

        {/* Password with strength indicator — controlled via Controller */}
        <div>
          <label htmlFor="signup-password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Password
          </label>
          <div className="relative">
            <Controller
              name="password"
              control={control}
              rules={{
                required: 'Password is required.',
                minLength: { value: 8, message: 'Password must be at least 8 characters.' },
              }}
              render={({ field }) => (
                <input
                  {...field}
                  id="signup-password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="••••••••"
                  className={`${errors.password ? inputErr : inputOk} pr-10`}
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

          {/* Password strength bar */}
          {passwordValue.length > 0 && (
            <div className="mt-2 space-y-1">
              <div className="flex gap-1">
                {[1, 2, 3].map((step) => (
                  <div
                    key={step}
                    className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
                      strength.score >= step ? strength.barColor : 'bg-gray-200 dark:bg-gray-600'
                    }`}
                  />
                ))}
              </div>
              <p className={`text-xs font-medium ${strength.textColor}`}>{strength.label}</p>
            </div>
          )}

          {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
        </div>

        {/* Confirm password — controlled via Controller, client-side only */}
        <div>
          <label htmlFor="confirm-password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Confirm password
          </label>
          <div className="relative">
            <Controller
              name="confirmPassword"
              control={control}
              rules={{
                required: 'Please confirm your password.',
                validate: (val) => val === passwordValue || 'Passwords do not match.',
              }}
              render={({ field }) => (
                <input
                  {...field}
                  id="confirm-password"
                  type={showConfirm ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="••••••••"
                  className={`${errors.confirmPassword ? inputErr : inputOk} pr-10`}
                />
              )}
            />
            <button
              type="button"
              onClick={() => setShowConfirm((v) => !v)}
              className="absolute inset-y-0 right-0 flex items-center px-3 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 focus:outline-none"
              aria-label={showConfirm ? 'Hide password' : 'Show password'}
            >
              {showConfirm ? <EyeOffIcon /> : <EyeIcon />}
            </button>
          </div>
          {errors.confirmPassword && (
            <p className="mt-1 text-xs text-red-500">{errors.confirmPassword.message}</p>
          )}
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
          {loading ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="text-center text-sm text-gray-500 dark:text-gray-400">
        Already have an account?{' '}
        <Link to="/signin" className="font-medium text-violet-600 hover:text-violet-500 dark:text-violet-400">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
