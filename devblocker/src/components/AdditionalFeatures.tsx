function AdditionalFeatures() {
  return (
    <section className="bg-white py-16 md:py-24">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Continue your Skype conversations in Teams
          </h2>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div className="text-center">
            <div className="text-5xl mb-4">📹</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Video calls</h3>
            <p className="text-gray-600">Connect face-to-face</p>
          </div>
          
          <div className="text-center">
            <div className="text-5xl mb-4">🤝</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Collaboration tools</h3>
            <p className="text-gray-600">Share, play, shop together</p>
          </div>
          
          <div className="text-center">
            <div className="text-5xl mb-4">📄</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Files</h3>
            <p className="text-gray-600">Share documents and more</p>
          </div>
          
          <div className="text-center">
            <div className="text-5xl mb-4">🎨</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Chat themes</h3>
            <p className="text-gray-600">Reflect your style</p>
          </div>
        </div>
        
        <div className="mt-12 text-center">
          <p className="text-lg text-gray-700 mb-4">
            Share meeting links with anyone on any device
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button className="bg-blue-600 text-white px-6 py-3 rounded-md font-medium hover:bg-blue-700 transition-colors">
              Download the Teams app
            </button>
          </div>
        </div>
      </div>
    </section>
  )
}

export default AdditionalFeatures

