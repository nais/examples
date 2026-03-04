import { NextResponse } from 'next/server';
import { isEnabled, FEATURE_FLAGS } from '@/utils/unleash';

export async function GET() {
  const features = Object.fromEntries(
    Object.entries(FEATURE_FLAGS).map(([key, flag]) => [flag, isEnabled(flag)])
  );
  return NextResponse.json(features);
}
