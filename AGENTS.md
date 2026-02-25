# AGENTS.md — Nais Examples

This repository is a polyglot microservices demo for the [NAIS platform](https://nais.io). Four services communicate over HTTP, deployed with NAIS manifests, and instrumented with OpenTelemetry.

## Architecture

```text
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ quotes-frontend │────▶│ quotes-backend  │◀────│quotes-analytics │
│   Next.js 16    │     │  Kotlin/Ktor 3  │     │   .NET 10       │
│   Port 3000     │     │   Port 8080     │     │   Port 8081     │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                        ┌────────▼────────┐
                        │  PostgreSQL 16  │
                        └─────────────────┘

quotes-loadgen (Go) generates traffic against the frontend.
```

- Service-to-service URLs use plain HTTP without ports: `http://quotes-backend`, `http://quotes-analytics`
- NAIS `accessPolicy` in `.nais/app.yaml` controls allowed traffic
- OpenTelemetry auto-instrumentation — do not add manual instrumentation in production code

## Commands

```bash
# Full CI for all services (lint → test → build)
mise run ci

# Per-service CI
cd quotes-frontend && mise run ci
cd quotes-backend  && mise run ci
cd quotes-analytics && mise run ci
cd quotes-loadgen  && mise run ci

# Start everything locally (requires Docker for infra)
mise run dev

# Start infra only, run services manually
mise run dev:manual
```

## Service Details

| Service          | Language                   | Build                    | Test             | Lint                                |
| ---------------- | -------------------------- | ------------------------ | ---------------- | ----------------------------------- |
| quotes-frontend  | TypeScript (Next.js 16)    | `yarn build`             | `vitest run`     | `eslint .`                          |
| quotes-backend   | Kotlin 2 (Ktor 3, Exposed) | `./gradlew build`        | `./gradlew test` | `./gradlew ktlintCheck`             |
| quotes-analytics | C# (.NET 10)               | `dotnet build`           | `dotnet test`    | `dotnet format --verify-no-changes` |
| quotes-loadgen   | Go 1.24                    | `go build ./cmd/main.go` | `go test ./...`  | `golangci-lint run`                 |

## Rules

### Always

- Run `mise run ci` (or per-service equivalent) before considering work done
- Follow existing patterns in each service — match the style you see, not a generic style guide
- Use environment variables for configuration; never hardcode secrets or URLs
- Keep service URLs portable: `http://quotes-backend` (no ports, no localhost)
- Update `.nais/app.yaml` when adding env vars, changing ports, or modifying health paths
- Use `ConcurrentDictionary` (not `Dictionary`) for any shared mutable state in .NET
- Escape user input in SQL — e.g. `%` and `_` in LIKE clauses (backend uses Exposed ORM)
- Write deterministic tests — mock external HTTP calls, don't accept multiple status codes

### Ask First

- Adding new dependencies — justify why, check if an existing dep already covers it
- Changing Dockerfile base images or OpenTelemetry versions
- Modifying CI workflows (`.github/workflows/`)
- Altering `accessPolicy` in `.nais/app.yaml`

### Never

- Add manual OpenTelemetry instrumentation — NAIS auto-instrumentation handles this
- Commit `.env` files, secrets, or credentials
- Hardcode `localhost` or port numbers in service-to-service calls
- Skip tests to make CI pass

## Language-Specific Patterns

### TypeScript (quotes-frontend)

- Path alias: `@/` maps to `./src/`
- API routes live in `src/app/api/` (Next.js App Router)
- Tests in `src/__tests__/` using Vitest + React Testing Library
- `output: 'standalone'` in `next.config.js` — required for the distroless Docker image
- Use `pino` for logging (server-side via `@navikt/next-logger`)

```typescript
// ✅ Import from path alias
import { getQuotes } from "@/utils/apiClient";

// ❌ Relative paths across directories
import { getQuotes } from "../../../utils/apiClient";
```

### Kotlin (quotes-backend)

- JVM toolchain: Java 21
- Ktor 3 with kotlinx.serialization (not Jackson)
- Database: Exposed ORM with PostgreSQL (H2 for tests)
- Tests use `testApplication { }` from Ktor test host

```kotlin
// ✅ Escape LIKE wildcards from user input
val escaped = query.replace("%", "\\%").replace("_", "\\_")
QuotesTable.select { QuotesTable.text like "%$escaped%" }

// ❌ Raw user input in SQL
QuotesTable.select { QuotesTable.text like "%$query%" }
```

### C# (quotes-analytics)

- .NET 10 with top-level `Program.cs`
- Tests: xUnit + FluentAssertions + Moq
- `HttpClient` injected via `AddHttpClient<QuotesAnalyticsService>`
- Mock the `HttpMessageHandler` in tests, not the `HttpClient`

```csharp
// ✅ Thread-safe cache
private readonly ConcurrentDictionary<string, QuoteAnalytics> _cache = new();

// ❌ Not thread-safe under concurrent requests
private readonly Dictionary<string, QuoteAnalytics> _cache = new();
```

### Go (quotes-loadgen)

- Standard project layout: `cmd/` for entrypoint, `internal/` for packages
- Uses Cobra for CLI
- `CGO_ENABLED=0` for static binaries in Docker

## File Structure Conventions

```text
quotes-<service>/
├── .mise.toml              # Tool versions + tasks (dev, test, ci, build)
├── .nais/app.yaml          # NAIS deployment manifest
├── Dockerfile              # Multi-stage build
├── README.md               # Service-specific docs
└── ...                     # Language-specific source layout
```

- Each service is self-contained with its own `.mise.toml`, `Dockerfile`, and `.nais/app.yaml`
- Root `.mise.toml` orchestrates cross-service tasks (`mise run dev`, `mise run ci`)
- `docker-compose.yaml` provides local infrastructure (PostgreSQL, Unleash, Grafana/OTEL)
