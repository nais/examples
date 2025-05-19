import { NextResponse } from 'next/server';
import { Quote } from '@/types/quote';
import { v4 as uuidv4 } from 'uuid';
import { quotes } from '@/data/quotes';

import logger from '@/utils/logger';

export async function GET() {
  const isFailture = Math.random() < 0.1;
  if (isFailture) {
    logger.error('Internal Server Error occurred while fetching a random quote');
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }

  const randomQuote = quotes[Math.floor(Math.random() * quotes.length)];
  logger.info({ event: 'GET_RANDOM_QUOTE', quote: randomQuote }, 'Random quote fetched successfully');
  return NextResponse.json(randomQuote);
}

export async function POST(request: Request) {
  try {
    const { text, author } = await request.json();

    if (!text) {
      logger.warn('Attempt to add a quote without text');
      return NextResponse.json({ error: 'Quote text is required.' }, { status: 400 });
    }

    const newQuote: Quote = {
      id: uuidv4(),
      text,
      author: author || 'Unknown',
    };

    quotes.push(newQuote);

    logger.info({ event: 'ADD_QUOTE', quote: newQuote }, 'New quote added successfully');
    return NextResponse.json({ message: 'Quote added successfully!', quote: newQuote });
  } catch (error) {
    logger.error({ event: 'ADD_QUOTE_ERROR', error }, 'Error adding quote');
    return NextResponse.json({ error: 'Failed to add quote.' }, { status: 500 });
  }
}
