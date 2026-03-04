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

export function isEnabled(flag: string, defaultValue = true): boolean {
  const client = getUnleash();
  if (!client) return defaultValue;
  return client.isEnabled(flag, undefined, defaultValue);
}

export const FEATURE_FLAGS = {
  QUOTES_SUBMIT: 'quotes.submit',
  QUOTES_ERRORS: 'quotes.errors',
} as const;
