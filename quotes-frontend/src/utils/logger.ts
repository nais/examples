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
      const span = trace.getSpan(context.active());
      if (span) {
        const spanContext = span.spanContext();
        return {
          trace_id: spanContext.traceId,
          span_id: spanContext.spanId,
          trace_flags: `0${spanContext.traceFlags.toString(16)}`,
        };
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
