import React from "react";
import { HandThumbUpIcon, HandThumbDownIcon, ArrowPathIcon, ClipboardIcon } from '@heroicons/react/24/solid';
import { Quote } from "@/types/quote";

interface QuoteDisplayProps {
  quote: Quote;
  handleThumbsUp: () => void;
  handleThumbsDown: () => void;
  fetchQuote: () => void;
  disableRandomQuote?: boolean;
  disableNewQuote?: boolean;
}

const QuoteDisplay: React.FC<QuoteDisplayProps> = ({
  quote,
  handleThumbsUp,
  handleThumbsDown,
  fetchQuote,
  disableRandomQuote = false,
  disableNewQuote = false,
}) => {
  const copyLink = () => {
    const absoluteUrl = `${window.location.origin}/quotes/${quote.id}`;
    navigator.clipboard.writeText(absoluteUrl).then(() => {
      alert('Quote link copied to clipboard!');
    });
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-200 p-4">
      <div className="bg-gray-50 shadow-lg rounded-lg p-8 max-w-2xl text-center">
        <h1 className="text-4xl font-bold mb-6 text-gray-800">Nais Quote</h1>
        <p className="text-2xl italic text-gray-600 mb-2">{quote.text}</p>
        <p className="text-lg text-gray-500">- {quote.author}</p>
        <div className="flex justify-center gap-4 mt-6">
          <div className="inline-flex rounded-md shadow-sm" role="group">
            <button
              onClick={handleThumbsUp}
              className="flex items-center justify-center bg-gray-800 text-white px-6 py-3 rounded-l-lg hover:bg-gray-900"
            >
              <HandThumbUpIcon className="h-5 w-5" />
            </button>
            <button
              onClick={handleThumbsDown}
              className="flex items-center justify-center bg-gray-800 text-white px-6 py-3 rounded-r-lg hover:bg-gray-900"
            >
              <HandThumbDownIcon className="h-5 w-5" />
            </button>
          </div>
          <button
            onClick={fetchQuote}
            disabled={disableRandomQuote}
            className={`flex items-center justify-center px-6 py-3 rounded-lg ${disableRandomQuote ? 'bg-gray-400 cursor-not-allowed' : 'bg-gray-800 text-white hover:bg-gray-900'}`}
          >
            <ArrowPathIcon className="h-5 w-5" />
          </button>
          <a
            href="/submit-quote"
            className={`flex items-center justify-center px-6 py-3 rounded-lg ${disableNewQuote ? 'bg-gray-400 cursor-not-allowed pointer-events-none' : 'bg-gray-800 text-white hover:bg-gray-900'}`}
          >
            Submit a New Quote
          </a>
          <button
            onClick={copyLink}
            className="flex items-center justify-center bg-gray-800 text-white px-6 py-3 rounded-lg hover:bg-gray-900"
          >
            <ClipboardIcon className="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default QuoteDisplay;
