/**
 * Files Page
 * Upload, download, and manage files
 */

import React, { useState, useEffect } from 'react';
import { filesApi } from '../api';
import type { FileMetadata } from '../types/api';

interface FilesPageProps {
  channelId?: number;
  teamId?: number;
}

export const FilesPage: React.FC<FilesPageProps> = ({ channelId }) => {
  const [files, setFiles] = useState<FileMetadata[]>([]);
  const [selectedChannelId, setSelectedChannelId] = useState<number | null>(channelId || null);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');

  // Update selectedChannelId when prop changes
  useEffect(() => {
    if (channelId) {
      setSelectedChannelId(channelId);
    }
  }, [channelId]);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !selectedChannelId) return;

    setIsUploading(true);
    setError('');

    try {
      const uploadedFile = await filesApi.uploadFile({
        file,
        channelId: selectedChannelId,
      });
      setFiles((prev) => [uploadedFile, ...prev]);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to upload file');
      console.error('Error uploading file:', err);
    } finally {
      setIsUploading(false);
      // Reset file input
      e.target.value = '';
    }
  };

  const handleDownload = async (fileId: number, filename: string) => {
    try {
      const blob = await filesApi.downloadFile(fileId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to download file');
      console.error('Error downloading file:', err);
    }
  };

  const handleLock = async (fileId: number) => {
    try {
      await filesApi.lockFile(fileId);
      setFiles((prev) =>
        prev.map((f) => (f.id === fileId ? { ...f, lockedBy: 1 } : f)) // Assuming current user ID is 1
      );
    } catch (err: any) {
      console.error('Error locking file:', err);
    }
  };

  const handleUnlock = async (fileId: number) => {
    try {
      await filesApi.unlockFile(fileId);
      setFiles((prev) =>
        prev.map((f) => (f.id === fileId ? { ...f, lockedBy: undefined } : f))
      );
    } catch (err: any) {
      console.error('Error unlocking file:', err);
    }
  };

  useEffect(() => {
    if (selectedChannelId) {
      const fetchFiles = async () => {
        setIsLoading(true);
        try {
          const fileList = await filesApi.getChannelFiles(selectedChannelId);
          setFiles(fileList);
        } catch (err: any) {
          setError(err.response?.data?.error || 'Failed to load files');
          console.error('Error fetching files:', err);
        } finally {
          setIsLoading(false);
        }
      };

      fetchFiles();
    }
  }, [selectedChannelId]);

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Files</h1>
          <p className="text-gray-600 mt-1">Manage and share files</p>
        </div>
        <div className="flex items-center space-x-4">
          <input
            type="number"
            placeholder="Channel ID"
            value={selectedChannelId || ''}
            onChange={(e) => setSelectedChannelId(e.target.value ? Number(e.target.value) : null)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
          <label className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 cursor-pointer transition-colors">
            {isUploading ? 'Uploading...' : '+ Upload File'}
            <input
              type="file"
              onChange={handleFileUpload}
              disabled={isUploading || !selectedChannelId}
              className="hidden"
            />
          </label>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {/* Files List */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        {!selectedChannelId ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📁</div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">Select a Channel</h2>
            <p className="text-gray-600">Enter a channel ID to view and upload files</p>
          </div>
        ) : isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        ) : files.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📄</div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">No Files Yet</h2>
            <p className="text-gray-600 mb-6">Upload your first file to get started</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {files.map((file) => (
              <div key={file.id} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4 flex-1 min-w-0">
                    <div className="text-3xl">📄</div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-sm font-medium text-gray-900 truncate">{file.filename}</h3>
                      <div className="flex items-center space-x-4 mt-1 text-xs text-gray-500">
                        <span>{formatFileSize(file.size)}</span>
                        <span>{file.contentType}</span>
                        <span>Uploaded {new Date(file.uploadedAt).toLocaleDateString()}</span>
                        {file.lockedBy && (
                          <span className="text-orange-600">🔒 Locked</span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    {file.lockedBy ? (
                      <button
                        onClick={() => handleUnlock(file.id)}
                        className="px-3 py-1 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                      >
                        Unlock
                      </button>
                    ) : (
                      <button
                        onClick={() => handleLock(file.id)}
                        className="px-3 py-1 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                      >
                        Lock
                      </button>
                    )}
                    <button
                      onClick={() => handleDownload(file.id, file.filename)}
                      className="px-3 py-1 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                      Download
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

