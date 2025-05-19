"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Quote } from "@/types/quote";
import QuoteDisplay from "@/components/QuoteDisplay";
import logger from '@/utils/logger';

export default function QuotePage() {
  const [quote, setQuote] = useState<Quote | null>(null);
  const params = useParams();
  const id = params?.id;

  useEffect(() => {
    if (id) {
      fetch(`/api/quotes/${id}`)
        .then((response) => {
          if (response.status === 404) {
            setQuote({
              id: "",
              text: "Quote not found. Please check the URL or try another quote.",
              author: ""
            });
            return null;
          }

          if (!response.ok) {
            throw new Error(`Error: ${response.status} - ${response.statusText}`);
          }
          return response.json();
        })
        .then((data) => setQuote(data))
        .catch((error) => {
          logger.error("Failed to fetch quote:", error);
          setQuote({
            id: "",
            text: "Failed to load quote. Please try again later.",
            author: ""
          });
        });
    }
  }, [id]);

  if (!quote) {
    return <p>Loading...</p>;
  }

  if (!quote.id) {
    return <p>{quote.text}</p>;
  }

  return (
    <QuoteDisplay
      quote={quote}
      handleThumbsUp={() => console.log("Thumbs up for:", quote)}
      handleThumbsDown={() => console.log("Thumbs down for:", quote)}
      fetchQuote={() => { }}
      disableRandomQuote={true}
      disableNewQuote={false}
    />
  );
}
