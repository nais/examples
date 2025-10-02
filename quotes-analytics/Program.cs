using System.Diagnostics;
using System.Diagnostics.Metrics;
using Nais.QuotesAnalytics.Services;
using OpenTelemetry.Logs;
using OpenTelemetry.Metrics;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;

var builder = WebApplication.CreateBuilder(args);

var serviceName = "quotes-analytics";
var serviceVersion = "1.0.0";

builder.Services.AddOpenTelemetry()
    .ConfigureResource(resource => resource
        .AddService(serviceName: serviceName, serviceVersion: serviceVersion))
    .WithTracing(tracing => tracing
        .AddAspNetCoreInstrumentation(options =>
        {
            options.RecordException = true;
            options.EnrichWithHttpRequest = (activity, httpRequest) =>
            {
                activity.SetTag("http.request.path", httpRequest.Path);
                activity.SetTag("http.request.method", httpRequest.Method);
            };
        })
        .AddHttpClientInstrumentation(options =>
        {
            options.RecordException = true;
            options.EnrichWithHttpRequestMessage = (activity, httpRequestMessage) =>
            {
                activity.SetTag("http.client.url", httpRequestMessage.RequestUri?.ToString());
            };
        })
        .AddSource(QuotesAnalyticsService.ActivitySourceName)
        .AddOtlpExporter(options =>
        {
            options.Endpoint = new Uri(builder.Configuration["Otel:Endpoint"] ?? "http://localhost:4317");
        }))
    .WithMetrics(metrics => metrics
        .AddAspNetCoreInstrumentation()
        .AddHttpClientInstrumentation()
        .AddRuntimeInstrumentation()
        .AddMeter(QuotesAnalyticsService.MeterName)
        .AddOtlpExporter(options =>
        {
            options.Endpoint = new Uri(builder.Configuration["Otel:Endpoint"] ?? "http://localhost:4317");
        }));

builder.Logging.AddOpenTelemetry(logging =>
{
    logging.AddOtlpExporter(options =>
    {
        options.Endpoint = new Uri(builder.Configuration["Otel:Endpoint"] ?? "http://localhost:4317");
    });
});

builder.Services.AddHttpClient<QuotesAnalyticsService>(client =>
{
    var backendUrl = builder.Configuration["QuotesBackend:Url"] ?? "http://localhost:8080";
    client.BaseAddress = new Uri(backendUrl);
});
builder.Services.AddSingleton<QuotesAnalyticsService>();
builder.Services.AddControllers();

var app = builder.Build();

app.MapControllers();

app.MapGet("/internal/health", () => Results.Ok(new { status = "healthy", service = serviceName }))
    .WithName("Health");

app.MapGet("/internal/ready", () => Results.Ok(new { status = "ready", service = serviceName }))
    .WithName("Ready");

app.Run();

public partial class Program { }
