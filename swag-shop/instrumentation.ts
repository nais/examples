import { registerOTel } from '@vercel/otel';

export async function register() {
  // registerOTel('next-app');
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    await import('./instrumentation.node');
  }
}