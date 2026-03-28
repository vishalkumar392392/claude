/**
 * AuthLayout — a centered card wrapper used by both SignIn and SignUp pages.
 */
export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 py-12">
      <div className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-8 space-y-6">
        {/* App branding */}
        <div className="text-center">
          <h1 className="text-3xl font-semibold tracking-tight text-gray-900 dark:text-white">
            Shopline
          </h1>
        </div>
        {children}
      </div>
    </div>
  )
}
