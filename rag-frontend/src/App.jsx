import { useState } from 'react';
import UploadPanel from './components/UploadPanel';
import ChatWindow from './components/ChatWindow';
import ChatInput from './components/ChatInput';

function App() {
  const [messages, setMessages] = useState([]);
  const [isUploaded, setIsUploaded] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleReset = () => {
    setIsUploaded(false);
    setMessages([]);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 shadow-sm">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-slate-900 rounded-lg flex items-center justify-center">
              <span className="text-white font-semibold text-sm">DM</span>
            </div>
            <div>
              <h1 className="text-lg font-semibold text-slate-900">DocuMind</h1>
              <p className="text-xs text-slate-500">Document Q&A Assistant</p>
            </div>
          </div>

          {isUploaded && (
            <button
              onClick={handleReset}
              className="text-sm text-slate-500 hover:text-slate-900 transition-colors flex items-center gap-1.5"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              New Document
            </button>
          )}
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-6 py-8 flex flex-col">
        {!isUploaded ? (
          <UploadPanel onUploadSuccess={() => setIsUploaded(true)} />
        ) : (
          <div className="flex-1 flex flex-col bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
            <ChatWindow messages={messages} isLoading={isLoading} />
            <ChatInput
              messages={messages}
              setMessages={setMessages}
              isLoading={isLoading}
              setIsLoading={setIsLoading}
            />
          </div>
        )}
      </main>
    </div>
  );
}

export default App;