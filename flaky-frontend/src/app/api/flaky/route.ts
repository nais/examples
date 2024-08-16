import getConfig from 'next/config';
import type { NextRequest, NextResponse } from 'next/server';
import pino from 'pino';
import { trace, context } from '@opentelemetry/api'
import { getLoggerWithTraceContext } from '@/lib/logger';

const { serverRuntimeConfig: c } = getConfig();

export async function GET(request: NextRequest) {
  const log = getLoggerWithTraceContext(context.active());

  log.info({ traceparent: request.headers.get('traceparent') }, 'GET /api/flaky');

  log.info('Calling flaky service');
  return await fetch(c.flakyServiceUrl, { cache: 'no-store' })
    .then((response: Response) => {
      log.info({ response: { status: response.status, statusText: response.statusText } }, 'Flaky service response');
      return response;
    }).catch((error) => {
      log.error({ error: { cause: error?.cause } }, 'Flaky service error');
      return new Response(
        JSON.stringify({
          error: error?.cause?.message || "Internal Server Error",
        }),
        {
          status: 500,
          statusText: "Internal Server Error",
        },
      );
    });
}