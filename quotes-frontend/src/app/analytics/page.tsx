'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { ChartBarIcon, HomeIcon } from '@heroicons/react/24/solid';

interface QuoteAnalytics {
  id: string;
  quoteId: string;
  wordCount: number;
  sentimentScore: number;
  category: string;
  analyzedAt: string;
}

interface AnalyticsSummary {
  totalQuotes: number;
  averageWordCount: number;
  averageCharacterCount: number;
  averageSentimentScore: number;
  categoryDistribution: { [key: string]: number };
  mostCommonCategory: string;
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<QuoteAnalytics[]>([]);
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [analyticsRes, summaryRes] = await Promise.all([
          fetch('/api/analytics'),
          fetch('/api/analytics/summary')
        ]);

        if (analyticsRes.ok) {
          const analyticsData = await analyticsRes.json();
          setAnalytics(analyticsData);
        }

        if (summaryRes.ok) {
          const summaryData = await summaryRes.json();
          setSummary(summaryData);
        }

        if (!analyticsRes.ok && !summaryRes.ok) {
          setError('Failed to fetch analytics data. Make sure the analytics service is running.');
        }
      } catch (err) {
        console.error('Error fetching analytics data:', err);
        setError('Failed to fetch analytics data. Make sure the analytics service is running.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-lg">Loading analytics...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-8">
          <ChartBarIcon className="mx-auto h-12 w-12 text-blue-600" />
          <h1 className="mt-4 text-3xl font-bold text-gray-900">Quote Analytics</h1>
          <p className="mt-2 text-lg text-gray-600">
            Insights and analysis of quote data
          </p>
        </div>

        <div className="mb-6">
          <Link
            href="/"
            className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <HomeIcon className="h-4 w-4 mr-2" />
            Back to Quotes
          </Link>
        </div>

        {error && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4 mb-6">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-yellow-800">
                  Analytics Service Unavailable
                </h3>
                <div className="mt-2 text-sm text-yellow-700">
                  <p>{error}</p>
                  <p className="mt-1">
                    The analytics service provides insights about quote sentiment, word count, and categorization.
                    It uses OpenTelemetry for custom traces and metrics.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {summary && (
          <div className="bg-white overflow-hidden shadow rounded-lg mb-8">
            <div className="px-4 py-5 sm:p-6">
              <h2 className="text-lg font-medium text-gray-900 mb-4">Summary Statistics</h2>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-blue-50 p-4 rounded-lg">
                  <dt className="text-sm font-medium text-blue-600">Total Quotes</dt>
                  <dd className="text-2xl font-semibold text-blue-900">{summary.totalQuotes}</dd>
                </div>
                <div className="bg-green-50 p-4 rounded-lg">
                  <dt className="text-sm font-medium text-green-600">Avg Word Count</dt>
                  <dd className="text-2xl font-semibold text-green-900">{summary.averageWordCount.toFixed(1)}</dd>
                </div>
                <div className="bg-purple-50 p-4 rounded-lg">
                  <dt className="text-sm font-medium text-purple-600">Avg Sentiment</dt>
                  <dd className="text-2xl font-semibold text-purple-900">{summary.averageSentimentScore.toFixed(2)}</dd>
                </div>
                <div className="bg-orange-50 p-4 rounded-lg">
                  <dt className="text-sm font-medium text-orange-600">Avg Characters</dt>
                  <dd className="text-2xl font-semibold text-orange-900">{summary.averageCharacterCount.toFixed(1)}</dd>
                </div>
              </div>

              {Object.keys(summary.categoryDistribution).length > 0 && (
                <div className="mt-6">
                  <h3 className="text-md font-medium text-gray-900 mb-3">Category Distribution</h3>
                  <div className="mb-3 text-sm text-gray-600">
                    Most common category: <span className="font-semibold text-gray-900">{summary.mostCommonCategory}</span>
                  </div>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                    {Object.entries(summary.categoryDistribution).map(([category, count]) => (
                      <div key={category} className={`p-3 rounded text-center ${category === summary.mostCommonCategory ? 'bg-blue-100 border-2 border-blue-300' : 'bg-gray-50'}`}>
                        <div className="text-sm font-medium text-gray-600 capitalize">{category}</div>
                        <div className="text-lg font-semibold text-gray-900">{count}</div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {analytics.length > 0 && (
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            <div className="px-4 py-5 sm:px-6">
              <h2 className="text-lg font-medium text-gray-900">Individual Quote Analytics</h2>
              <p className="mt-1 text-sm text-gray-500">
                Detailed analysis for each quote including sentiment and categorization
              </p>
            </div>
            <ul className="divide-y divide-gray-200">
              {analytics.map((item) => (
                <li key={item.id} className="px-4 py-4 sm:px-6">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-blue-600">
                          Quote ID: {item.quoteId}
                        </p>
                        <div className="flex space-x-4 text-sm text-gray-500">
                          <span>{item.wordCount} words</span>
                          <span>Sentiment: {item.sentimentScore.toFixed(2)}</span>
                          <span className="capitalize bg-gray-100 px-2 py-1 rounded">
                            {item.category}
                          </span>
                        </div>
                      </div>
                      <p className="text-xs text-gray-400 mt-1">
                        Analyzed: {new Date(item.analyzedAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}

        {!loading && !error && analytics.length === 0 && (
          <div className="text-center py-12">
            <ChartBarIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No analytics data</h3>
            <p className="mt-1 text-sm text-gray-500">
              Analytics will appear here once quotes are processed by the analytics service.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}