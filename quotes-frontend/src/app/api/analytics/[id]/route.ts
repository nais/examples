import { NextResponse } from 'next/server';
import logger from '@/utils/logger';
import type { NextRequest } from 'next/server';

// Use runtime environment variable for server-side API calls
const getAnalyticsBaseUrl = () => {
  return process.env.ANALYTICS_SERVICE_URL || 'http://localhost:8081';
};

export async function GET(
  request: NextRequest,
  {
    params,
  }: {
    params: Promise<{ id: string }>;
  }
) {
  try {
    const { id } = await params;
    const baseUrl = getAnalyticsBaseUrl();
    logger.info({ event: 'QUOTE_ANALYTICS_REQUEST', id, baseUrl }, 'Fetching quote analytics from service');

    const response = await fetch(`${baseUrl}/api/analytics/${id}`, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      logger.warn({ event: 'QUOTE_ANALYTICS_SERVICE_ERROR', id, status: response.status, error: errorText }, 'Analytics service returned error');

      if (response.status === 404) {
        return NextResponse.json({
          error: 'Analytics not found for this quote.',
          quoteId: id
        }, { status: 404 });
      }

      throw new Error(`Analytics service error: ${response.status} - ${errorText}`);
    }

    const analytics = await response.json();
    logger.info({ event: 'GET_QUOTE_ANALYTICS_SUCCESS', id, analytics }, 'Quote analytics fetched successfully');
    return NextResponse.json(analytics);
  } catch (error) {
    logger.error({ event: 'GET_QUOTE_ANALYTICS_ERROR', error }, 'Error fetching quote analytics');
    return NextResponse.json({
      error: 'Failed to fetch quote analytics. Analytics service may be unavailable.'
    }, { status: 500 });
  }
}