import { NextResponse } from 'next/server';
import { isEnabled, FEATURE_FLAGS } from '@/utils/unleash';

export async function GET() {
  const features = Object.fromEntries(
    Object.entries(FEATURE_FLAGS).map(([, flag]) => [
      flag,
      isEnabled(flag as (typeof FEATURE_FLAGS)[keyof typeof FEATURE_FLAGS]),
    ])
  );
  return NextResponse.json(features);
}
