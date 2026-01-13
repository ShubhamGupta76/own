import { Link, useLocation } from 'react-router-dom'
import { useState } from 'react'

function JiraHeader() {
  const location = useLocation()
  const [showFeaturesMenu, setShowFeaturesMenu] = useState(false)
  const [showSolutionsMenu, setShowSolutionsMenu] = useState(false)

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-8">
            <Link to="/" className="text-xl font-semibold text-blue-600">
              DevBlocker
            </Link>
            <div className="flex items-center space-x-4">
              <Link
                to="/teams"
                className="font-medium transition-colors text-gray-700 hover:text-gray-900"
              >
                Teams
              </Link>
            </div>
            
            <div className="relative"
              onMouseEnter={() => setShowFeaturesMenu(true)}
              onMouseLeave={() => setShowFeaturesMenu(false)}
            >
              <button className="text-gray-700 hover:text-gray-900 font-medium flex items-center">
                Features
                <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              {showFeaturesMenu && (
                <div className="absolute top-full left-0 mt-2 w-48 bg-white rounded-md shadow-lg border border-gray-200 py-2">
                  <Link to="/jira/features" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">All Features</Link>
                  <Link to="/jira/rovo" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">Rovo in Jira</Link>
                </div>
              )}
            </div>

            <div className="relative"
              onMouseEnter={() => setShowSolutionsMenu(true)}
              onMouseLeave={() => setShowSolutionsMenu(false)}
            >
              <button className="text-gray-700 hover:text-gray-900 font-medium flex items-center">
                Solutions
                <svg className="ml-1 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              {showSolutionsMenu && (
                <div className="absolute top-full left-0 mt-2 w-64 bg-white rounded-md shadow-lg border border-gray-200 py-2">
                  <div className="px-4 py-2 text-xs font-semibold text-gray-500 uppercase">Teams</div>
                  <Link to="/jira/solutions/marketing" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">Marketing</Link>
                  <Link to="/jira/solutions/engineering" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">Engineering</Link>
                  <Link to="/jira/solutions/design" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">Design</Link>
                  <Link to="/jira/solutions/operations" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">Operations</Link>
                  <Link to="/jira/solutions/it" className="block px-4 py-2 text-gray-700 hover:bg-gray-100">IT</Link>
                </div>
              )}
            </div>

            <Link to="/jira/guide" className="text-gray-700 hover:text-gray-900 font-medium">
              Guide
            </Link>
            <Link to="/jira/templates" className="text-gray-700 hover:text-gray-900 font-medium">
              Templates
            </Link>
            <Link to="/jira/pricing" className="text-gray-700 hover:text-gray-900 font-medium">
              Pricing
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            <button className="text-gray-700 hover:text-gray-900 font-medium">
              Sign in
            </button>
            <Link
              to="/jira"
              className="bg-blue-600 text-white px-4 py-2 rounded-md font-medium hover:bg-blue-700 transition-colors"
            >
              Get it free
            </Link>
          </div>
        </div>
      </nav>
    </header>
  )
}

export default JiraHeader

