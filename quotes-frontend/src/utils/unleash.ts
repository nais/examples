import { initialize, Unleash } from 'unleash-client';

let unleash: Unleash | null = null;

export function getUnleash(): Unleash | null {
  if (unleash) return unleash;

  const url = process.env.UNLEASH_SERVER_API_URL;
  const token = process.env.UNLEASH_SERVER_API_TOKEN;
  if (!url || !token) return null;

  unleash = initialize({
    url: `${url}/`,
    appName: 'quotes-frontend',
    customHeaders: { Authorization: token },
    environment: process.env.UNLEASH_SERVER_API_ENVIRONMENT || 'development',
  });

  return unleash;
}

export const FEATURE_FLAGS = {
  QUOTES_SUBMIT: 'quotes.submit',
  QUOTES_ERRORS: 'quotes.errors',
} as const;

export type FeatureFlag = (typeof FEATURE_FLAGS)[keyof typeof FEATURE_FLAGS];

const FEATURE_FLAG_DEFAULTS: Record<FeatureFlag, boolean> = {
  [FEATURE_FLAGS.QUOTES_SUBMIT]: true,
  [FEATURE_FLAGS.QUOTES_ERRORS]: false,
};

export function isEnabled(flag: FeatureFlag, defaultValue?: boolean): boolean {
  const effectiveDefault =
    defaultValue !== undefined ? defaultValue : FEATURE_FLAG_DEFAULTS[flag] ?? true;

  const client = getUnleash();
  if (!client) return effectiveDefault;
  return client.isEnabled(flag, undefined, effectiveDefault);
}
