# Quotes Analytics Service (.NET)

.NET 8 service providing quote analytics with OpenTelemetry auto-instrumentation and custom telemetry.

## Features

- Analyzes quotes for word count, sentiment, and categorization
- Custom OpenTelemetry spans and metrics (5 metrics, 7 spans)
- In-memory caching for performance
- RESTful API: `/api/analytics` (all/single/summary), `/internal/health`, `/internal/ready`

See [INSTRUMENTATION.md](INSTRUMENTATION.md) for telemetry details

## Quick Start

**Prerequisites:** [mise](https://mise.jdx.dev/) or .NET 8 SDK, Docker

```bash
# Using mise (recommended)
mise install                    # Install .NET 8
mise run build                  # Build
mise run test                   # Run tests
mise run run                    # Start service

# Or with .NET CLI
dotnet restore quotes-analytics.sln
dotnet build quotes-analytics.sln
dotnet run

# Start with Docker Compose
docker-compose up -d quotes-analytics
```

Service runs at `http://localhost:8081`

## Development Commands

```bash
mise run test              # All tests
mise run test:unit         # Unit tests only
mise run test:integration  # Integration tests
mise run test:coverage     # With coverage
mise run watch             # Hot reload
mise run lint              # Format & build checks
mise run ci                # Full CI pipeline
```

Run `mise tasks` to see all available commands.

## Deployment

Automatically deployed to NAIS dev-gcp on push to main. Config: `.nais/app.yaml` (auto-instrumentation enabled, 1-2 replicas, 128-256Mi memory)

## Observability

Grafana at `http://localhost:3000`: Explore → Tempo (traces), Prometheus (`quotes_*` metrics), Loki (logs)
