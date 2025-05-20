import { NextResponse } from 'next/server';
import { getQuotes, createQuote, getQuoteById } from '@/utils/apiClient';
import logger from '@/utils/logger';

export async function GET() {
  try {
    const quotes = await getQuotes();
    if (quotes.length === 0) {
      logger.warn('No quotes available to select');
      return NextResponse.json({ error: 'No quotes available.' }, { status: 404 });
    }

    const randomQuote = quotes[Math.floor(Math.random() * quotes.length)];
    logger.info({ event: 'GET_RANDOM_QUOTE', randomQuote }, 'Random quote selected');

    const fullQuote = await getQuoteById(randomQuote.id!);
    logger.info({ event: 'GET_QUOTE_BY_ID', fullQuote }, 'Full quote fetched successfully');
    return NextResponse.json(fullQuote);
  } catch (error) {
    logger.error({ event: 'GET_RANDOM_QUOTE_ERROR', error }, 'Error fetching random quote');
    return NextResponse.json({ error: 'Failed to fetch random quote.' }, { status: 500 });
  }
}

export async function POST(request: Request) {
  try {
    const { text, author } = await request.json();

    if (!text) {
      logger.warn('Attempt to add a quote without text');
      return NextResponse.json({ error: 'Quote text is required.' }, { status: 400 });
    }

    const newQuote = await createQuote({ text, author });
    logger.info({ event: 'ADD_QUOTE', quote: newQuote }, 'New quote added successfully');
    return NextResponse.json({ message: 'Quote added successfully!', quote: newQuote });
  } catch (error) {
    logger.error({ event: 'ADD_QUOTE_ERROR', error }, 'Error adding quote');
    return NextResponse.json({ error: 'Failed to add quote.' }, { status: 500 });
  }
}
