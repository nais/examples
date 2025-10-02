import { NextResponse } from 'next/server';
import logger from '@/utils/logger';

// Use runtime environment variable for server-side API calls
const getAnalyticsBaseUrl = () => {
  return process.env.ANALYTICS_SERVICE_URL || 'http://localhost:8081';
};

export async function GET() {
  try {
    const baseUrl = getAnalyticsBaseUrl();
    logger.info({ event: 'ANALYTICS_REQUEST', baseUrl }, 'Fetching analytics from service');

    const response = await fetch(`${baseUrl}/api/analytics`, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      logger.error({ event: 'ANALYTICS_SERVICE_ERROR', status: response.status, error: errorText }, 'Analytics service returned error');

      if (response.status === 404) {
        return NextResponse.json({ error: 'No analytics data available' }, { status: 404 });
      }

      throw new Error(`Analytics service error: ${response.status} - ${errorText}`);
    }

    const analytics = await response.json();
    logger.info({ event: 'GET_ANALYTICS_SUCCESS', count: analytics.length }, 'Analytics data fetched successfully');
    return NextResponse.json(analytics);
  } catch (error) {
    logger.error({ event: 'GET_ANALYTICS_ERROR', error }, 'Error fetching analytics');
    return NextResponse.json({
      error: 'Failed to fetch analytics. Analytics service may be unavailable.'
    }, { status: 500 });
  }
}