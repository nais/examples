// https://github.com/vercel/next.js/issues/47660#issuecomment-1796913537
import type { SpanContext } from '@opentelemetry/api';
import { isSpanContextValid, trace, context } from '@opentelemetry/api';

const getSpanContext = (): SpanContext | undefined => {
  const span = trace.getSpan(context.active());
  if (!span) {
    return undefined;
  }
  const spanContext = span.spanContext();
  if (!isSpanContextValid(spanContext)) {
    return undefined;
  }
  return spanContext;
};

export const getTraceId = (): string | undefined => {
  const spanContext = getSpanContext();
  return spanContext?.traceId;
};

export const getSpanId = (): string | undefined => {
  const spanContext = getSpanContext();
  return spanContext?.spanId;
};

export const getTraceparentHeader = () => {
  const spanContext = getSpanContext();
  if (!spanContext) return '';

  return `00-${spanContext.traceId}-${spanContext.spanId}-01`;
};