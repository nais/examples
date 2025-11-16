import pino, { Logger } from "pino";
import { trace, context } from '@opentelemetry/api';

const isDevelopment = process.env.NODE_ENV === 'development';

const createLogger = (): Logger => {
  const baseOptions: pino.LoggerOptions = {
    messageKey: "message",
    errorKey: "error",
    formatters: {
      level: (label) => ({ level: label }),
    },
    mixin() {
      if (typeof window === 'undefined') {
        const span = trace.getSpan(context.active());
        if (span) {
          const spanContext = span.spanContext();
          return {
            trace_id: spanContext.traceId,
            span_id: spanContext.spanId,
            trace_flags: spanContext.traceFlags.toString(16).padStart(2, '0'),
          };
        }
      }
      return {};
    },
  };

  if (isDevelopment) {
    return pino({
      ...baseOptions,
      level: 'debug',
      transport: {
        target: 'pino-pretty',
        options: {
          colorize: true,
          translateTime: 'HH:MM:ss Z',
          ignore: 'pid,hostname',
        },
      },
    });
  }

  return pino({
    ...baseOptions,
    level: 'info',
  });
};

export const logger: Logger = createLogger();

export default logger;
