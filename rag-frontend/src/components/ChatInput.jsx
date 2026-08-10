import { useState } from 'react';

function ChatInput({ messages, setMessages, isLoading, setIsLoading }) {
  const [input, setInput] = useState('');

  const handleSend = async () => {
    const question = input.trim();
    if (!question || isLoading) return;

    setInput('');
    setMessages([...messages, { role: 'user', content: question }]);
    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8081/api/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question }),
      });

      if (!response.ok) throw new Error('Request failed');

      const answer = await response.text();
      setMessages((prev) => [...prev, { role: 'assistant', content: answer }]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'Sorry, something went wrong. Please try again.' },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="border-t border-slate-200 p-4">
      <div className="flex items-end gap-2">
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask a question about your document..."
          rows={1}
          disabled={isLoading}
          className="flex-1 resize-none rounded-xl border border-slate-300 px-4 py-3 text-sm
            focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent
            disabled:bg-slate-50 disabled:text-slate-400"
        />
        <button
          onClick={handleSend}
          disabled={isLoading || !input.trim()}
          className="bg-slate-900 text-white rounded-xl px-4 py-3 text-sm font-medium
            hover:bg-slate-800 transition-colors
            disabled:bg-slate-200 disabled:text-slate-400 disabled:cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </button>
      </div>
      <p className="text-xs text-slate-400 mt-2 text-center">
        Answers are generated based on your uploaded document
      </p>
    </div>
  );
}

export default ChatInput;