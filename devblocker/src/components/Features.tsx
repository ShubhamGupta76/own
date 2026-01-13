interface FeaturesProps {
  view?: 'activity' | 'calendar' | 'calls' | 'files' | 'default'
}

function Features({ view = 'default' }: FeaturesProps) {
  // Activity View
  if (view === 'activity') {
    return (
      <div className="flex-1 overflow-y-auto p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Activity</h2>
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map((item) => (
            <div key={item} className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
              <div className="flex items-start space-x-3">
                <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white">
                  👤
                </div>
                <div className="flex-1">
                  <p className="text-gray-900">
                    <span className="font-semibold">John Doe</span> mentioned you in <span className="text-blue-600">#general</span>
                  </p>
                  <p className="text-sm text-gray-500 mt-1">2 hours ago</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  // Calendar View
  if (view === 'calendar') {
    return (
      <div className="flex-1 overflow-y-auto p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Calendar</h2>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
            New Event
          </button>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <div className="grid grid-cols-7 gap-2 mb-4">
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
              <div key={day} className="text-center font-semibold text-gray-700 p-2">{day}</div>
            ))}
          </div>
          <div className="grid grid-cols-7 gap-2">
            {Array.from({ length: 35 }, (_, i) => {
              const date = i + 1
              const isToday = date === new Date().getDate()
              return (
                <div
                  key={i}
                  className={`p-2 h-16 border border-gray-200 rounded ${
                    isToday ? 'bg-blue-50 border-blue-500' : 'hover:bg-gray-50'
                  }`}
                >
                  <span className={`text-sm ${isToday ? 'font-bold text-blue-600' : 'text-gray-700'}`}>
                    {date > 0 && date <= 31 ? date : ''}
                  </span>
                </div>
              )
            })}
          </div>
        </div>
        <div className="mt-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Upcoming Events</h3>
          <div className="space-y-3">
            {[
              { title: 'Team Standup', time: '10:00 AM', date: 'Today' },
              { title: 'Client Meeting', time: '2:00 PM', date: 'Tomorrow' },
              { title: 'Sprint Planning', time: '11:00 AM', date: 'Dec 15' }
            ].map((event, idx) => (
              <div key={idx} className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{event.title}</p>
                    <p className="text-sm text-gray-500">{event.date} at {event.time}</p>
                  </div>
                  <button className="text-blue-600 hover:text-blue-700 text-sm">Join</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  // Calls View
  if (view === 'calls') {
    return (
      <div className="flex-1 overflow-y-auto p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Calls</h2>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
            New Call
          </button>
        </div>
        <div className="space-y-4">
          {[
            { name: 'John Doe', type: 'Missed', time: '2 hours ago', avatar: '👤' },
            { name: 'Jane Smith', type: 'Outgoing', time: 'Yesterday', avatar: '👤' },
            { name: 'Bob Johnson', type: 'Incoming', time: '2 days ago', avatar: '👤' },
            { name: 'Team Call', type: 'Group', time: '3 days ago', avatar: '👥' }
          ].map((call, idx) => (
            <div key={idx} className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center text-white text-xl">
                    {call.avatar}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900">{call.name}</p>
                    <p className="text-sm text-gray-500">
                      <span className={call.type === 'Missed' ? 'text-red-600' : ''}>{call.type}</span> • {call.time}
                    </p>
                  </div>
                </div>
                <button className="text-blue-600 hover:text-blue-700 p-2">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  // Files View
  if (view === 'files') {
    return (
      <div className="flex-1 overflow-y-auto p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Files</h2>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
            Upload
          </button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { name: 'Document.pdf', size: '2.4 MB', modified: '2 hours ago', type: 'pdf' },
            { name: 'Presentation.pptx', size: '5.1 MB', modified: 'Yesterday', type: 'ppt' },
            { name: 'Spreadsheet.xlsx', size: '1.2 MB', modified: '2 days ago', type: 'xls' },
            { name: 'Image.png', size: '856 KB', modified: '3 days ago', type: 'img' },
            { name: 'Video.mp4', size: '45.2 MB', modified: '1 week ago', type: 'video' },
            { name: 'Archive.zip', size: '12.3 MB', modified: '2 weeks ago', type: 'zip' }
          ].map((file, idx) => (
            <div key={idx} className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer">
              <div className="text-4xl mb-3">
                {file.type === 'pdf' && '📄'}
                {file.type === 'ppt' && '📊'}
                {file.type === 'xls' && '📈'}
                {file.type === 'img' && '🖼️'}
                {file.type === 'video' && '🎥'}
                {file.type === 'zip' && '📦'}
              </div>
              <p className="font-semibold text-gray-900 truncate mb-1">{file.name}</p>
              <p className="text-sm text-gray-500">{file.size}</p>
              <p className="text-xs text-gray-400 mt-2">{file.modified}</p>
            </div>
          ))}
        </div>
      </div>
    )
  }

  // Default view - Teams Free Landing Page
  return (
    <div className="flex-1 overflow-y-auto bg-white">
      {/* Hero Section */}
      <section className="bg-white py-12 md:py-20">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-gray-900 mb-6 leading-tight">
              Video calls with anyone, anytime
            </h1>
            <p className="text-xl md:text-2xl text-gray-600 mb-10 max-w-2xl mx-auto">
              Connect and collaborate for free in Teams
            </p>
            <p className="text-lg text-gray-700 mb-8 max-w-2xl mx-auto">
              Connect with anyone, anytime
            </p>
            <p className="text-base text-gray-600 mb-10 max-w-2xl mx-auto">
              Sign in with your Skype account to meet and chat with anyone on Teams for free.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-3 justify-center items-center mb-8">
              <button className="bg-blue-600 text-white px-8 py-3 rounded-md font-semibold hover:bg-blue-700 transition-colors text-base whitespace-nowrap">
                Start a meeting for free
              </button>
              <button className="bg-white text-blue-600 border border-blue-600 px-8 py-3 rounded-md font-semibold hover:bg-blue-50 transition-colors text-base whitespace-nowrap">
                Open Teams in your browser
              </button>
              <button className="text-blue-600 font-semibold hover:underline text-base whitespace-nowrap">
                Download the Teams app
              </button>
            </div>
            
            <div className="mb-12">
              <a href="#join-meeting" className="text-gray-700 hover:text-gray-900 font-semibold underline text-base">
                Join a meeting
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Join Meeting Section */}
      <section id="join-meeting" className="bg-white py-16 md:py-20">
        <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-gray-50 rounded-lg p-8 md:p-12">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-3 text-center">
              Join with a meeting ID
            </h2>
            <p className="text-sm text-gray-600 mb-8 text-center">
              Each field should consist of alphanumeric characters only.
            </p>
            <form className="space-y-6 max-w-md mx-auto">
              <div>
                <label htmlFor="meetingId" className="block text-sm font-semibold text-gray-900 mb-2">
                  Meeting ID <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="meetingId"
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-base"
                  placeholder="Enter meeting ID"
                  required
                  pattern="[A-Za-z0-9]+"
                />
              </div>
              <div>
                <label htmlFor="passcode" className="block text-sm font-semibold text-gray-900 mb-2">
                  Meeting passcode <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="passcode"
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-base"
                  placeholder="Enter passcode"
                  required
                  pattern="[A-Za-z0-9]+"
                />
              </div>
              <button
                type="submit"
                className="w-full bg-blue-600 text-white px-6 py-3 rounded-md font-semibold hover:bg-blue-700 transition-colors text-base"
              >
                Join meeting
              </button>
            </form>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="bg-white py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 md:gap-10">
            {[
              {
                title: 'Share meeting links with anyone on any device',
                description: 'Collaborate together on Teams video calls',
                icon: '🔗'
              },
              {
                title: 'Meet for free up to 60 mins',
                description: 'Instant or schedule ahead',
                icon: '⏱️'
              },
              {
                title: 'View Live captions',
                description: 'In over 40 languages',
                icon: '💬'
              },
              {
                title: 'Entertain the crowd',
                description: 'With interactive emoji',
                icon: '😊'
              },
              {
                title: 'Share your screen',
                description: 'For live collab and play',
                icon: '📺'
              },
              {
                title: 'Choose backgrounds and views to set the scene',
                description: 'Customize your meeting experience',
                icon: '🎨'
              }
            ].map((feature, index) => (
              <div
                key={index}
                className="bg-white border border-gray-200 rounded-lg p-6 md:p-8 hover:shadow-lg transition-shadow"
              >
                <div className="text-5xl mb-4">{feature.icon}</div>
                <h3 className="text-xl md:text-2xl font-bold text-gray-900 mb-3 leading-tight">
                  {feature.title}
                </h3>
                <p className="text-base text-gray-600 leading-relaxed">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Continue Skype Section */}
      <section className="bg-white py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
              Continue your Skype conversations in Teams
            </h2>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 mb-16">
            <div className="text-center">
              <div className="text-6xl mb-5">📹</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">Video calls</h3>
              <p className="text-base text-gray-600">Connect face-to-face</p>
            </div>
            
            <div className="text-center">
              <div className="text-6xl mb-5">🤝</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">Collaboration tools</h3>
              <p className="text-base text-gray-600">Share, play, shop together</p>
            </div>
            
            <div className="text-center">
              <div className="text-6xl mb-5">📄</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">Files</h3>
              <p className="text-base text-gray-600">Share documents and more</p>
            </div>
            
            <div className="text-center">
              <div className="text-6xl mb-5">🎨</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-3">Chat themes</h3>
              <p className="text-base text-gray-600">Reflect your style</p>
            </div>
          </div>
          
          <div className="text-center pt-8 border-t border-gray-200">
            <p className="text-xl text-gray-700 mb-6 font-medium">
              Share meeting links with anyone on any device
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button className="bg-blue-600 text-white px-8 py-3 rounded-md font-semibold hover:bg-blue-700 transition-colors text-base">
                Download the Teams app
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}

export default Features
