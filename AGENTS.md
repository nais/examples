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
| quotes-frontend  | TypeScript (Next.js 16)    | `pnpm build`             | `vitest run`     | `eslint .`                          |
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

## Feature Flags (Unleash)

Feature flags use [Unleash](https://docs.nais.io/services/feature-flagging/) via the NAIS platform.

**Architecture:**

- Each service has `.nais/unleash.yaml` that provisions an API token as a Kubernetes secret
- `envFrom` in `.nais/app.yaml` injects `UNLEASH_SERVER_API_URL`, `UNLEASH_SERVER_API_TOKEN`, `UNLEASH_SERVER_API_ENVIRONMENT`
- Backend uses `io.getunleash:unleash-client-java`, frontend uses `unleash-client` (Node.js)
- When Unleash is unavailable, flags fall back to their configured default values (graceful degradation)

**Current flags:**

- `quotes.submit` — gates the "Submit a New Quote" feature (backend + frontend)
- `quotes.errors` — enables simulated error injection in the backend (default: **disabled**)

**Patterns:**

```kotlin
// Kotlin: check flag, default true when Unleash is unavailable
FeatureFlags.isEnabled(FeatureFlags.QUOTES_SUBMIT)

// Kotlin: error injection flag, default false (opt-in)
FeatureFlags.isEnabled(FeatureFlags.QUOTES_ERRORS, default = false)

// Kotlin: get variant for A/B testing
val variant = FeatureFlags.getVariant(FeatureFlags.QUOTES_SUBMIT)
if (variant.name != "disabled") { /* use variant */ }
```

```typescript
// TypeScript (server-side API routes): check flag
import { isEnabled, getVariant, FEATURE_FLAGS } from '@/utils/unleash';
isEnabled(FEATURE_FLAGS.QUOTES_SUBMIT);

// TypeScript: get variant
const variant = getVariant(FEATURE_FLAGS.QUOTES_SUBMIT);
```

**Impression data:**

- Impression data and usage metrics are sent to Unleash automatically by the SDK
- Enable impression data per-toggle in the Unleash admin UI — metrics appear in the Unleash "Metrics" tab
- `GET /api/features` returns `{ "flag": { "enabled": true, "variant": { "name": "..." } } }`

**Rules:**

- Register flag names as constants in `FeatureFlags.kt` / `unleash.ts`
- Define per-flag defaults (e.g., `quotes.submit` defaults `true`, `quotes.errors` defaults `false`)
- Use `GET /api/features` (backend or frontend) to inspect flag states and variants
- Local Unleash admin UI: `http://localhost:4242` (started via `mise run infra:up`)
