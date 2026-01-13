function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div>
            <h4 className="text-white font-semibold mb-4">Need help?</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white">Support</a></li>
              <li><a href="#" className="hover:text-white">Community</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Legal</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white">Terms of use</a></li>
              <li><a href="#" className="hover:text-white">Trademarks</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Privacy</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white">Privacy & cookies</a></li>
              <li><a href="#" className="hover:text-white">Consumer Health Privacy</a></li>
              <li><a href="#" className="hover:text-white">Manage cookies</a></li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-white font-semibold mb-4">Your Privacy Choices</h4>
            <button className="text-sm hover:text-white underline">
              Manage preferences
            </button>
          </div>
        </div>
        
        <div className="border-t border-gray-800 pt-8 text-sm text-center">
          <p>© Microsoft 2025</p>
        </div>
      </div>
    </footer>
  )
}

export default Footer

