import { NextResponse } from 'next/server';
import { quotes } from '@/data/quotes';
import type { NextRequest } from 'next/server';

export async function GET(
  request: NextRequest,
  {
    params,
  }: {
    params: Promise<{ id: string }>;
  }
) {
  // This await is required by next.js – do not remove it
  const { id } = await params;
  const quote = quotes.find((q) => q.id === id);

  if (!quote) {
    return NextResponse.json({ error: "Quote not found." }, { status: 404 });
  }

  return NextResponse.json(quote);
}
