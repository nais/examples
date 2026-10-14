'use client';

import { useEffect, useState } from "react";
import { Quote } from '@/types/quote';
import QuoteDisplay from '@/components/QuoteDisplay';
import logger from '@/utils/logger';

export default function Home() {
  const [quote, setQuote] = useState<Quote>({ id: "", text: "Loading...", author: "" });
  const [submitEnabled, setSubmitEnabled] = useState(true);

  const fetchQuote = async () => {
    try {
      const response = await fetch('/api/quotes');
      if (!response.ok) throw new Error(`Error: ${response.statusText}`);
      const data: Quote = await response.json();
      logger.info({ event: 'FETCH_QUOTE', quote: data }, 'Quote fetched successfully');
      setQuote({ id: data.id, text: data.text, author: data.author });
    } catch (error) {
      logger.error({ event: 'FETCH_QUOTE_ERROR', error }, 'Failed to fetch quote');
      setQuote({ id: "", text: 'Failed to fetch quote. Please try again later.', author: "" });
    }
  };

  useEffect(() => {
    let cancelled = false;

    const loadFeatures = async () => {
      try {
        const res = await fetch('/api/features');
        const features = await res.json();
        if (!cancelled) setSubmitEnabled(features['quotes.submit'] ?? true);
      } catch {
        if (!cancelled) setSubmitEnabled(true);
      }
    };

    const loadQuote = async () => {
      try {
        const pathSegments = window.location.pathname.split('/');
        const quoteId = pathSegments[pathSegments.length - 1];

        if (quoteId && quoteId !== '') {
          const response = await fetch(`/api/quotes/${quoteId}`);
          const data = await response.json();
          if (!cancelled) setQuote({ id: data.id, text: data.text, author: data.author });
        } else {
          await fetchQuote();
        }
      } catch (error) {
        console.error('Failed to fetch quote by ID:', error);
        if (!cancelled) await fetchQuote();
      }
    };

    void loadFeatures();
    void loadQuote();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleThumbsUp = () => {
    console.log('Thumbs up for:', quote);
  };

  const handleThumbsDown = () => {
    console.log('Thumbs down for:', quote);
  };

  return (
    <QuoteDisplay
      quote={quote}
      handleThumbsUp={handleThumbsUp}
      handleThumbsDown={handleThumbsDown}
      fetchQuote={fetchQuote}
      disableRandomQuote={false}
      disableNewQuote={!submitEnabled}
    />
  );
}
