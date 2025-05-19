"use client";

import { useState } from "react";
import Link from "next/link";
import logger from "@/utils/logger";

export default function SubmitQuote() {
  const [text, setText] = useState("");
  const [author, setAuthor] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const response = await fetch("/api/quotes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ text, author }),
      });

      if (response.ok) {
        const newQuote = await response.json();
        logger.info(
          { event: "SUBMIT_QUOTE", quote: newQuote },
          "Quote submitted successfully"
        );
        window.location.href = `/quotes/${newQuote.quote.id}`;
      } else {
        logger.warn(
          { event: "SUBMIT_QUOTE_FAILED", status: response.status },
          "Failed to submit quote"
        );
        setMessage("Failed to submit the quote.");
      }
    } catch (error) {
      logger.error({ event: "SUBMIT_QUOTE_ERROR", error }, "Error submitting quote");
      setMessage("An error occurred. Please try again later.");
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-200 p-4">
      <div className="bg-gray-50 shadow-lg rounded-lg p-8 max-w-2xl w-full text-center">
        <form onSubmit={handleSubmit}>
          <div className="space-y-12">
            <div className="border-b border-gray-300 pb-12">
              <h2 className="text-4xl font-bold text-gray-800">
                Submit a New Quote
              </h2>
              <p className="mt-2 text-lg text-gray-600">
                Share your favorite quote with the world.
              </p>

              <div className="mt-10 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
                <div className="sm:col-span-4 sm:col-start-2">
                  <label
                    htmlFor="quote-text"
                    className="block text-lg font-medium text-gray-800"
                  >
                    Quote Text
                  </label>
                  <div className="mt-2 sm:col-start-2 sm:col-span-4">
                    <textarea
                      id="quote-text"
                      value={text}
                      onChange={(e) => setText(e.target.value)}
                      placeholder="Enter the quote text"
                      className="block w-full rounded-md bg-white px-4 py-2 text-lg text-gray-800 border border-gray-300 placeholder:text-gray-400 focus:ring-gray-800 focus:border-gray-800"
                      required
                    />
                  </div>
                </div>

                <div className="sm:col-span-4 sm:col-start-2">
                  <label
                    htmlFor="quote-author"
                    className="block text-lg font-medium text-gray-800"
                  >
                    Author
                  </label>
                  <div className="mt-2">
                    <input
                      id="quote-author"
                      type="text"
                      value={author}
                      onChange={(e) => setAuthor(e.target.value)}
                      placeholder="Enter the author's name"
                      className="block w-full rounded-md bg-white px-4 py-2 text-lg text-gray-800 border border-gray-300 placeholder:text-gray-400 focus:ring-gray-800 focus:border-gray-800"
                    />
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 flex items-center justify-end gap-x-6">
              <Link
                href="/"
                className="text-lg font-semibold text-gray-800 hover:underline"
              >
                Cancel
              </Link>
              <button
                type="submit"
                className="rounded-md bg-gray-800 px-6 py-3 text-lg font-semibold text-white shadow-md hover:bg-gray-900 focus:ring-2 focus:ring-offset-2 focus:ring-gray-800"
              >
                Submit Quote
              </button>
            </div>
          </div>
        </form>
        {message && (
          <p className="mt-4 text-center text-gray-700 text-lg">{message}</p>
        )}
      </div>
    </div>
  );
}
