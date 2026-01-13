import { Link } from 'react-router-dom'

function JiraFooter() {
  return (
    <footer className="bg-gray-900 text-gray-300 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div>
            <h4 className="text-white font-semibold mb-4">Products</h4>
            <ul className="space-y-2">
              <li><Link to="/jira/rovo" className="hover:text-white">Rovo</Link></li>
              <li><Link to="/jira" className="hover:text-white">Jira</Link></li>
              <li><Link to="/jira/align" className="hover:text-white">Jira Align</Link></li>
              <li><Link to="/jira/service" className="hover:text-white">Jira Service Management</Link></li>
              <li><Link to="/jira/confluence" className="hover:text-white">Confluence</Link></li>
              <li><Link to="/jira/trello" className="hover:text-white">Trello</Link></li>
              <li><Link to="/jira/bitbucket" className="hover:text-white">Bitbucket</Link></li>
              <li><Link to="/jira/products" className="hover:text-white">See all products</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Resources</h4>
            <ul className="space-y-2">
              <li><Link to="/jira/support" className="hover:text-white">Technical support</Link></li>
              <li><Link to="/jira/licensing" className="hover:text-white">Purchasing & licensing</Link></li>
              <li><Link to="/jira/community" className="hover:text-white">Atlassian Community</Link></li>
              <li><Link to="/jira/knowledge" className="hover:text-white">Knowledge base</Link></li>
              <li><Link to="/jira/marketplace" className="hover:text-white">Marketplace</Link></li>
              <li><Link to="/jira/account" className="hover:text-white">My account</Link></li>
              <li><Link to="/jira/ticket" className="hover:text-white">Create support ticket</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Learn</h4>
            <ul className="space-y-2">
              <li><Link to="/jira/partners" className="hover:text-white">Partners</Link></li>
              <li><Link to="/jira/training" className="hover:text-white">Training & certification</Link></li>
              <li><Link to="/jira/docs" className="hover:text-white">Documentation</Link></li>
              <li><Link to="/jira/developer" className="hover:text-white">Developer resources</Link></li>
              <li><Link to="/jira/enterprise" className="hover:text-white">Enterprise services</Link></li>
              <li><Link to="/jira/resources" className="hover:text-white">See all resources</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-4">Company</h4>
            <ul className="space-y-2">
              <li><Link to="/jira/about" className="hover:text-white">About</Link></li>
              <li><Link to="/jira/careers" className="hover:text-white">Careers</Link></li>
              <li><Link to="/jira/events" className="hover:text-white">Events</Link></li>
              <li><Link to="/jira/blogs" className="hover:text-white">Blogs</Link></li>
              <li><Link to="/jira/investors" className="hover:text-white">Investor Relations</Link></li>
              <li><Link to="/jira/foundation" className="hover:text-white">Atlassian Foundation</Link></li>
              <li><Link to="/jira/press" className="hover:text-white">Press kit</Link></li>
              <li><Link to="/jira/contact" className="hover:text-white">Contact us</Link></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <p className="text-sm mb-4 md:mb-0">Copyright © 2025 Atlassian</p>
            <div className="flex flex-wrap gap-4 text-sm">
              <Link to="/jira/privacy" className="hover:text-white">Privacy policy</Link>
              <Link to="/jira/notice" className="hover:text-white">Notice at Collection</Link>
              <Link to="/jira/terms" className="hover:text-white">Terms</Link>
              <Link to="/jira/impressum" className="hover:text-white">Impressum</Link>
            </div>
            <div className="mt-4 md:mt-0">
              <select className="bg-gray-800 text-white border border-gray-700 rounded px-3 py-2">
                <option>English</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default JiraFooter

