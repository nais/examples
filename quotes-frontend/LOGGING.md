# Structured Logging with OpenTelemetry Integration

This document describes the structured logging implementation for the Next.js frontend using Pino logger fully integrated with OpenTelemetry.

## Overview

The quotes-frontend application now uses Pino for structured JSON logging with full OpenTelemetry integration for trace context propagation.

## Key Features

### 1. OpenTelemetry Auto-Instrumentation

- **Location**: `src/instrumentation.ts`
- Automatically instruments HTTP requests, database calls, and other operations
- Exports traces and metrics to OTLP endpoint
- Disabled file system instrumentation to reduce noise

### 2. Structured Logging with Trace Context

- **Location**: `src/utils/logger.ts`
- Uses Pino logger for structured JSON output
- Automatically includes OpenTelemetry trace context in all logs:
  - `trace_id`: Unique identifier for the entire request trace
  - `span_id`: Identifier for the current operation span
  - `trace_flags`: Sampling and other trace flags

### 3. Environment-Specific Configuration

**Development Mode:**
- Pretty-printed logs with colors
- Debug level logging
- Human-readable timestamps
- Excludes PID and hostname for cleaner output

**Production Mode:**
- JSON-formatted logs
- Info level logging
- Includes full trace context for correlation

## Usage Examples

### Basic Logging

```typescript
import logger from '@/utils/logger';

// Info log with context
logger.info({ event: 'USER_ACTION', userId: '123' }, 'User performed action');

// Warning log
logger.warn('Unusual condition detected');

// Error log
logger.error({ event: 'API_ERROR', error }, 'Failed to fetch data');
```

### Structured Fields

The logger automatically adds trace context when available:

```json
{
  "level": "info",
  "message": "Random quote selected",
  "event": "GET_RANDOM_QUOTE",
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "span_id": "00f067aa0ba902b7",
  "trace_flags": "01",
  "randomQuote": { "id": "1", "text": "..." }
}
```

## Configuration

### Environment Variables

- `OTEL_EXPORTER_OTLP_ENDPOINT`: OTLP endpoint URL (default: `http://localhost:4317`)
- `NODE_ENV`: Controls log format (`development` = pretty, `production` = JSON)

### Next.js Configuration

The `instrumentation.ts` file is automatically loaded by Next.js 15+ without requiring any special configuration in `next.config.js`.

## Benefits

1. **Distributed Tracing**: Correlate logs with traces across services
2. **Structured Data**: Easy to query and analyze in log aggregation systems
3. **Context Propagation**: Trace IDs flow through all logs in a request
4. **Auto-Instrumentation**: Minimal code changes, automatic metrics and traces
5. **Development Experience**: Pretty-printed logs during development

## Integration with NAIS Platform

In the NAIS platform, OpenTelemetry is auto-instrumented. The custom instrumentation in this application:

- Works seamlessly in both local development and NAIS environments
- Exports to the OTLP endpoint configured via environment variables
- Automatically propagates trace context to downstream services

## References

- [Pino Logger Documentation](https://getpino.io/)
- [OpenTelemetry JavaScript Documentation](https://opentelemetry.io/docs/languages/js/)
- [Structured Logging Blog Post](https://blog.arcjet.com/structured-logging-in-json-for-next-js/)
- [Next.js Instrumentation](https://nextjs.org/docs/app/building-your-application/optimizing/instrumentation)
