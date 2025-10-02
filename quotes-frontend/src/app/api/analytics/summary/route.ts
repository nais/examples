import { NextResponse } from 'next/server';
import logger from '@/utils/logger';

// Use runtime environment variable for server-side API calls
const getAnalyticsBaseUrl = () => {
  return process.env.ANALYTICS_SERVICE_URL || 'http://localhost:8081';
};

export async function GET() {
  try {
    const baseUrl = getAnalyticsBaseUrl();
    logger.info({ event: 'ANALYTICS_SUMMARY_REQUEST', baseUrl }, 'Fetching analytics summary from service');

    const response = await fetch(`${baseUrl}/api/analytics/summary`, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      logger.error({ event: 'ANALYTICS_SUMMARY_SERVICE_ERROR', status: response.status, error: errorText }, 'Analytics service returned error');

      if (response.status === 404) {
        return NextResponse.json({ error: 'No analytics summary available' }, { status: 404 });
      }

      throw new Error(`Analytics service error: ${response.status} - ${errorText}`);
    }

    const summary = await response.json();
    logger.info({ event: 'GET_ANALYTICS_SUMMARY_SUCCESS', summary }, 'Analytics summary fetched successfully');
    return NextResponse.json(summary);
  } catch (error) {
    logger.error({ event: 'GET_ANALYTICS_SUMMARY_ERROR', error }, 'Error fetching analytics summary');
    return NextResponse.json({
      error: 'Failed to fetch analytics summary. Analytics service may be unavailable.'
    }, { status: 500 });
  }
}