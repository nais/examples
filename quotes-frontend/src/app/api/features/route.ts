import { NextResponse } from 'next/server';
import { isEnabled, getVariant, FEATURE_FLAGS } from '@/utils/unleash';

export async function GET() {
  const features = Object.fromEntries(
    Object.entries(FEATURE_FLAGS).map(([, flag]) => {
      const flagKey = flag as (typeof FEATURE_FLAGS)[keyof typeof FEATURE_FLAGS];
      const variant = getVariant(flagKey);
      const detail: Record<string, unknown> = { enabled: isEnabled(flagKey) };
      if (variant.name !== 'disabled') {
        detail.variant = variant;
      }
      return [flag, detail];
    })
  );
  return NextResponse.json(features);
}
