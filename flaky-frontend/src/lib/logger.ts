import { Context, trace } from "@opentelemetry/api";
import pino, { Logger } from "pino";

export const logger: Logger = pino({
  base: undefined, // remove default fields
  formatters: {    // display level as a string
    level: (label) => {
      return {
        level: label
      }
    }
  }
});

export function getLoggerWithTraceContext(context: Context) {
  let current_span = trace.getSpan(context);
  let trace_id = current_span?.spanContext().traceId;
  let span_id = current_span?.spanContext().spanId;

  return logger.child({ trace_id, span_id });
}