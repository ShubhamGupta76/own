/**
 * Main Layout Component
 * Microsoft Teams-like 3-column layout:
 * 1. Primary Sidebar (icon navigation)
 * 2. Teams & Channels Sidebar (expandable teams)
 * 3. Main Content Area (with TopBar)
 */

import React from 'react';
import type { ReactNode } from 'react';
import { Outlet } from 'react-router-dom';
import { PrimarySidebar } from './PrimarySidebar';
import { TeamsChannelsSidebar } from './TeamsChannelsSidebar';
import { TopBar } from './TopBar';

interface MainLayoutProps {
  children?: ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="flex h-screen bg-white overflow-hidden">
      {/* Primary Sidebar - Icon Navigation */}
      <PrimarySidebar />

      {/* Teams & Channels Sidebar */}
      <TeamsChannelsSidebar />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        {/* Top Bar */}
        <TopBar />

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto bg-gray-50">
          {children || <Outlet />}
        </main>
      </div>
    </div>
  );
};

