import { NextResponse } from 'next/server';
import { getQuoteById } from '@/utils/apiClient';
import logger from '@/utils/logger';
import type { NextRequest } from 'next/server';

export async function GET(
  request: NextRequest,
  {
    params,
  }: {
    params: Promise<{ id: string }>;
  }
) {
  try {
    // This await is required by next.js – do not remove it
    const { id } = await params;
    logger.info({ event: 'GET_QUOTE_BY_ID', id }, 'Fetching quote by ID');

    const quote = await getQuoteById(id);

    logger.info({ event: 'GET_QUOTE_BY_ID_SUCCESS', quote }, 'Quote fetched successfully');
    return NextResponse.json(quote);
  } catch (error) {
    logger.error({ event: 'GET_QUOTE_BY_ID_ERROR', error }, 'Error fetching quote by ID');
    return NextResponse.json({ error: 'Quote not found.' }, { status: 404 });
  }
}
