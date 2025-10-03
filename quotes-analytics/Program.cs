using System.Diagnostics;
using System.Diagnostics.Metrics;
using Nais.QuotesAnalytics.Services;
using OpenTelemetry.Logs;
using OpenTelemetry.Metrics;
using OpenTelemetry.Trace;

var builder = WebApplication.CreateBuilder(args);

// In NAIS, OpenTelemetry is auto-instrumented. We only need to register our custom sources.
// For local development, we'll add minimal OTEL setup with OTLP exporter.
if (builder.Environment.IsDevelopment())
{
    builder.Services.AddOpenTelemetry()
        .WithTracing(tracing => tracing
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddSource(QuotesAnalyticsService.ActivitySourceName)
            .AddOtlpExporter())
        .WithMetrics(metrics => metrics
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddRuntimeInstrumentation()
            .AddMeter(QuotesAnalyticsService.MeterName)
            .AddOtlpExporter());

    builder.Logging.AddOpenTelemetry(logging => logging.AddOtlpExporter());
}
// In production (NAIS), auto-instrumentation handles everything automatically
// No manual OpenTelemetry configuration needed - just ensure ActivitySource and Meter are available

// Configuration
var backendUrl = builder.Configuration["QuotesBackend:Url"] ?? "http://localhost:8080";

// Log configuration in development only
if (builder.Environment.IsDevelopment())
{
    var logger = LoggerFactory.Create(b => b.AddConsole()).CreateLogger("Startup");
    logger.LogInformation("Analytics Service Configuration");
    logger.LogInformation("Environment: {Environment}", builder.Environment.EnvironmentName);
    logger.LogInformation("Backend URL: {BackendUrl}", backendUrl);
    logger.LogInformation("Custom ActivitySource: {ActivitySource}", QuotesAnalyticsService.ActivitySourceName);
    logger.LogInformation("Custom Meter: {Meter}", QuotesAnalyticsService.MeterName);
}

// Configure HttpClient for QuotesAnalyticsService
// Note: AddHttpClient<T> automatically registers T as transient
builder.Services.AddHttpClient<QuotesAnalyticsService>(client =>
{
    client.BaseAddress = new Uri(backendUrl);
    client.Timeout = TimeSpan.FromSeconds(30);
    client.DefaultRequestHeaders.Add("User-Agent", "QuotesAnalytics/1.0.0");
});
builder.Services.AddControllers();

// Add CORS for development and production
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        if (builder.Environment.IsDevelopment())
        {
            policy.WithOrigins("http://localhost:3000", "http://localhost:3001", "http://localhost:3002")
                  .AllowAnyHeader()
                  .AllowAnyMethod();
        }
        else
        {
            // In production (NAIS), services communicate internally via service mesh
            // Frontend requests go through ingress, so we allow the NAIS frontend domain
            policy.WithOrigins("https://*.nav.cloud.nais.io", "https://*.dev.nav.cloud.nais.io")
                  .AllowAnyHeader()
                  .AllowAnyMethod()
                  .SetIsOriginAllowedToAllowWildcardSubdomains();
        }
    });
});

var app = builder.Build();

// Log startup information in development only
if (app.Environment.IsDevelopment())
{
    var logger = app.Services.GetRequiredService<ILogger<Program>>();
    logger.LogInformation("Starting Analytics Service");
    logger.LogInformation("CORS Policy: AllowFrontend with origins: {Origins}",
        "http://localhost:3000, http://localhost:3001, http://localhost:3002");
}

// Enable CORS
app.UseCors("AllowFrontend");

app.MapControllers();

app.MapGet("/internal/health", () => Results.Ok(new { status = "healthy", service = "quotes-analytics" }))
    .WithName("Health");

app.MapGet("/internal/ready", () => Results.Ok(new { status = "ready", service = "quotes-analytics" }))
    .WithName("Ready");

app.Run();

public partial class Program { }
