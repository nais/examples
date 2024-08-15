import getConfig from 'next/config';
import type { NextRequest } from 'next/server';
import pino from 'pino';
import api from '@opentelemetry/api'

const { serverRuntimeConfig: c } = getConfig();

export async function GET(request: NextRequest) {
  let current_span = api.trace.getSpan(api.context.active());
  let trace_id = current_span?.spanContext().traceId;
  let span_id = current_span?.spanContext().spanId;

  const logger = pino({
    base: undefined,
    formatters: {
      level: (label) => {
        return {
          level: label
        }
      }
    }
  }).child({ trace_id, span_id });
  logger.info('Current span', current_span);
  logger.info('GET /api/flaky');

  logger.info('Calling flaky service');
  return await fetch(c.flakyServiceUrl, { cache: 'no-store' })
    .then((response) => {
      logger.info('Flaky service response', response);
      return response;
    }).catch((error) => {
      logger.error('Flaky service error', error);
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