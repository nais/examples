# OpenTelemetry Instrumentation Guide

This document provides examples and best practices for adding custom OpenTelemetry instrumentation in the Quotes Analytics service.

## Table of Contents

- [Custom Traces](#custom-traces)
- [Custom Metrics](#custom-metrics)
- [Structured Logging](#structured-logging)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

## Custom Traces

### Creating a Custom Span

```csharp
using var activity = ActivitySource.StartActivity("OperationName", ActivityKind.Internal);

try
{
    // Your business logic here
    var result = DoSomething();

    // Add tags/attributes to the span
    activity?.SetTag("operation.result", result);
    activity?.SetTag("operation.success", true);
}
catch (Exception ex)
{
    // Record error in span
    activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
    activity?.RecordException(ex);
    throw;
}
```

### Activity Kinds

Choose the appropriate `ActivityKind` for your span:

- `ActivityKind.Internal`: For internal operations (default)
- `ActivityKind.Server`: For handling incoming requests
- `ActivityKind.Client`: For making outgoing requests
- `ActivityKind.Producer`: For sending messages
- `ActivityKind.Consumer`: For receiving messages

```csharp
// Client span for HTTP request
using var activity = ActivitySource.StartActivity("FetchQuote", ActivityKind.Client);
activity?.SetTag("http.url", url);

var response = await httpClient.GetAsync(url);

activity?.SetTag("http.status_code", (int)response.StatusCode);
```

### Nested Spans

Create hierarchical traces by nesting spans:

```csharp
using var parentActivity = ActivitySource.StartActivity("ProcessQuotes", ActivityKind.Internal);
activity?.SetTag("quotes.count", quotes.Count);

foreach (var quote in quotes)
{
    using var childActivity = ActivitySource.StartActivity("ProcessSingleQuote", ActivityKind.Internal);
    childActivity?.SetTag("quote.id", quote.Id);

    await ProcessQuoteAsync(quote);
}
```

### Adding Events to Spans

```csharp
activity?.AddEvent(new ActivityEvent(
    "QuoteValidated",
    tags: new ActivityTagsCollection
    {
        { "validation.result", "passed" },
        { "validation.rules", 5 }
    }
));
```

## Custom Metrics

### Counter

Use counters for values that only increase:

```csharp
private readonly Counter<long> _requestCounter = Meter.CreateCounter<long>(
    "api.requests.total",
    unit: "requests",
    description: "Total number of API requests");

// Increment counter
_requestCounter.Add(1,
    new KeyValuePair<string, object?>("endpoint", "/api/analytics"),
    new KeyValuePair<string, object?>("method", "GET"));
```

### Histogram

Use histograms for distributions of values:

```csharp
private readonly Histogram<double> _requestDuration = Meter.CreateHistogram<double>(
    "api.request.duration",
    unit: "ms",
    description: "Request duration in milliseconds");

var stopwatch = Stopwatch.StartNew();
// ... do work ...
stopwatch.Stop();

_requestDuration.Record(stopwatch.ElapsedMilliseconds,
    new KeyValuePair<string, object?>("endpoint", "/api/analytics"),
    new KeyValuePair<string, object?>("status_code", 200));
```

### Observable Gauge

Use observable gauges for current values (sampled periodically):

```csharp
private int _activeConnections = 0;

private readonly ObservableGauge<int> _connectionsGauge = Meter.CreateObservableGauge(
    "active.connections",
    () => _activeConnections,
    unit: "connections",
    description: "Current number of active connections");
```

### Up/Down Counter

Use up/down counters for values that can increase or decrease:

```csharp
private readonly UpDownCounter<int> _queueSize = Meter.CreateUpDownCounter<int>(
    "queue.size",
    unit: "items",
    description: "Current size of the processing queue");

// Add to queue
_queueSize.Add(1, new KeyValuePair<string, object?>("queue.name", "analytics"));

// Remove from queue
_queueSize.Add(-1, new KeyValuePair<string, object?>("queue.name", "analytics"));
```

## Structured Logging

### Basic Logging with Context

```csharp
_logger.LogInformation(
    "Processing quote {QuoteId} from author {Author}",
    quote.Id,
    quote.Author);
```

### Logging with Additional Properties

```csharp
using (_logger.BeginScope(new Dictionary<string, object>
{
    ["UserId"] = userId,
    ["RequestId"] = requestId
}))
{
    _logger.LogInformation("Started processing user request");
    // All logs within this scope will include UserId and RequestId
    _logger.LogDebug("Validated input parameters");
}
```

### Log Levels

Choose appropriate log levels:

```csharp
_logger.LogTrace("Detailed trace information");        // Very detailed
_logger.LogDebug("Debug information");                 // Debugging
_logger.LogInformation("General information");         // Normal flow
_logger.LogWarning("Warning about potential issues");  // Warnings
_logger.LogError(ex, "An error occurred");            // Errors
_logger.LogCritical(ex, "Critical failure");          // Critical
```

## Error Handling

### Recording Exceptions in Spans

```csharp
try
{
    await RiskyOperation();
}
catch (Exception ex)
{
    activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
    activity?.RecordException(ex);

    _logger.LogError(ex, "Operation failed with error: {ErrorMessage}", ex.Message);

    // Optionally re-throw or handle
    throw;
}
```

### Conditional Error Recording

```csharp
var success = await TryOperation();

if (!success)
{
    activity?.SetStatus(ActivityStatusCode.Error, "Operation failed");
    activity?.SetTag("error.type", "validation_error");

    _logger.LogWarning("Operation did not succeed");
}
```

## Best Practices

### 1. Use Semantic Conventions

Follow OpenTelemetry semantic conventions for naming:

```csharp
// Good: Following conventions
activity?.SetTag("http.method", "GET");
activity?.SetTag("http.status_code", 200);
activity?.SetTag("http.url", url);

// Avoid: Custom naming
activity?.SetTag("method", "GET");
activity?.SetTag("response_status", 200);
```

### 2. Add Meaningful Tags

Include tags that help with debugging and analysis:

```csharp
activity?.SetTag("quote.id", quoteId);
activity?.SetTag("quote.author", author);
activity?.SetTag("quote.word_count", wordCount);
activity?.SetTag("processing.cache_hit", cacheHit);
```

### 3. Use High Cardinality Dimensions Carefully

Avoid high-cardinality values in metric dimensions:

```csharp
// Good: Low cardinality
_counter.Add(1, new KeyValuePair<string, object?>("endpoint", "/api/analytics"));

// Avoid: High cardinality (userId could have millions of values)
_counter.Add(1, new KeyValuePair<string, object?>("user_id", userId));
```

### 4. Set Appropriate Metric Boundaries

For histograms, consider the expected value range:

```csharp
private readonly Histogram<double> _latency = Meter.CreateHistogram<double>(
    "operation.latency",
    unit: "ms",
    description: "Operation latency",
    advice: new InstrumentAdvice<double>
    {
        HistogramBucketBoundaries = new[] { 10, 50, 100, 250, 500, 1000, 2500, 5000 }
    });
```

### 5. Clean Up Resources

Use `using` statements to ensure spans are properly closed:

```csharp
// Good: Automatic cleanup
using var activity = ActivitySource.StartActivity("Operation");
await DoWork();

// Avoid: Manual disposal (error-prone)
var activity = ActivitySource.StartActivity("Operation");
try
{
    await DoWork();
}
finally
{
    activity?.Dispose();
}
```

### 6. Avoid Sensitive Data

Never log or trace sensitive information:

```csharp
// Avoid: Sensitive data in logs/traces
_logger.LogInformation("User password: {Password}", password);
activity?.SetTag("user.email", email);

// Good: Sanitized or omitted
_logger.LogInformation("User authenticated successfully");
activity?.SetTag("user.id.hash", HashUserId(userId));
```

### 7. Use Sampling for High-Volume Operations

For very high-volume operations, consider sampling:

```csharp
// Only trace 10% of requests
if (Random.Shared.NextDouble() < 0.1)
{
    using var activity = ActivitySource.StartActivity("HighVolumeOperation");
    // ... operation ...
}
```

### 8. Correlate Logs with Traces

The correlation is automatic with OpenTelemetry, but you can enhance it:

```csharp
var traceId = Activity.Current?.TraceId.ToString();
var spanId = Activity.Current?.SpanId.ToString();

_logger.LogInformation(
    "Processing quote {QuoteId} [TraceId: {TraceId}, SpanId: {SpanId}]",
    quoteId,
    traceId,
    spanId);
```

## Example: Complete Instrumentation

Here's a complete example combining traces, metrics, and logs:

```csharp
public async Task<Result> ProcessQuoteAsync(string quoteId)
{
    var stopwatch = Stopwatch.StartNew();
    using var activity = ActivitySource.StartActivity("ProcessQuote", ActivityKind.Internal);
    activity?.SetTag("quote.id", quoteId);

    using (_logger.BeginScope(new Dictionary<string, object>
    {
        ["QuoteId"] = quoteId,
        ["Operation"] = "ProcessQuote"
    }))
    {
        try
        {
            _logger.LogInformation("Starting quote processing");

            // Fetch quote
            using (var fetchSpan = ActivitySource.StartActivity("FetchQuote", ActivityKind.Client))
            {
                var quote = await _httpClient.GetFromJsonAsync<Quote>($"/api/quotes/{quoteId}");
                fetchSpan?.SetTag("quote.found", quote != null);
            }

            // Process quote
            using (var processSpan = ActivitySource.StartActivity("AnalyzeQuote", ActivityKind.Internal))
            {
                var analysis = AnalyzeQuote(quote);
                processSpan?.SetTag("analysis.sentiment", analysis.Sentiment);
                processSpan?.SetTag("analysis.category", analysis.Category);
            }

            stopwatch.Stop();

            // Record metrics
            _processedCounter.Add(1,
                new KeyValuePair<string, object?>("status", "success"));

            _processingDuration.Record(stopwatch.ElapsedMilliseconds,
                new KeyValuePair<string, object?>("status", "success"));

            activity?.SetStatus(ActivityStatusCode.Ok);
            _logger.LogInformation(
                "Successfully processed quote in {Duration}ms",
                stopwatch.ElapsedMilliseconds);

            return Result.Success();
        }
        catch (Exception ex)
        {
            stopwatch.Stop();

            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            activity?.RecordException(ex);

            _processedCounter.Add(1,
                new KeyValuePair<string, object?>("status", "error"));

            _processingDuration.Record(stopwatch.ElapsedMilliseconds,
                new KeyValuePair<string, object?>("status", "error"));

            _logger.LogError(ex, "Failed to process quote");

            return Result.Failure(ex.Message);
        }
    }
}
```

## Resources

- [OpenTelemetry .NET Documentation](https://opentelemetry.io/docs/languages/net/)
- [Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/)
- [ASP.NET Core Instrumentation](https://github.com/open-telemetry/opentelemetry-dotnet-contrib/tree/main/src/OpenTelemetry.Instrumentation.AspNetCore)
