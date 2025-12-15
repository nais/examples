
# Quotes Backend

A Kotlin/Ktor microservice for managing inspirational quotes with PostgreSQL persistence, full CRUD operations, and OpenTelemetry observability.

## Features

- **Full CRUD Operations**: Create, read, update, and delete quotes
- **PostgreSQL Persistence**: Data survives application restarts with Cloud SQL integration
- **Search Functionality**: Search quotes by text or author
- **OpenAPI/Swagger Documentation**: Interactive API documentation
- **OpenTelemetry Observability**: Automatic instrumentation for logs, metrics, and traces
- **Audit Logging**: PostgreSQL audit logging with pgAudit (production)
- **Health & Readiness Probes**: Kubernetes-ready health checks
- **Comprehensive Testing**: Unit and integration tests with H2 in-memory database

## Quick Start

### Prerequisites

- [Java](https://www.java.com/) 21+
- [Gradle](https://gradle.org/) 8.12+
- [Docker](https://www.docker.com/) (for local database)
- [mise](https://mise.jdx.dev/) (optional, for task automation)

### Running Locally

```bash
# Start database and application
cd quotes-backend
mise run db:start        # Start PostgreSQL
mise run dev             # Start development server

# Or manually
docker-compose -f ../docker-compose.yaml up -d quotes-db
./gradlew run
```

The service starts on `http://localhost:8080`

## API Endpoints

### Quotes Management

| Method   | Endpoint                       | Description      |
| -------- | ------------------------------ | ---------------- |
| `GET`    | `/api/quotes`                  | Get all quotes   |
| `POST`   | `/api/quotes`                  | Create new quote |
| `GET`    | `/api/quotes/{id}`             | Get quote by ID  |
| `PUT`    | `/api/quotes/{id}`             | Update quote     |
| `DELETE` | `/api/quotes/{id}`             | Delete quote     |
| `GET`    | `/api/quotes/search?q={query}` | Search quotes    |

### Internal Endpoints

| Method | Endpoint           | Description                      |
| ------ | ------------------ | -------------------------------- |
| `GET`  | `/internal/health` | Health check                     |
| `GET`  | `/internal/ready`  | Readiness check (initializes DB) |
| `GET`  | `/internal/stats`  | Database statistics              |

### Documentation

- **Swagger UI**: `http://localhost:8080/swagger` - Interactive API testing
- **OpenAPI Spec**: `http://localhost:8080/openapi` - Machine-readable API spec

## Usage Examples

### Create Quote

```bash
curl -X POST http://localhost:8080/api/quotes \
  -H "Content-Type: application/json" \
  -d '{"text":"Innovation distinguishes between a leader and a follower.","author":"Steve Jobs"}'
```

### Get All Quotes

```bash
curl http://localhost:8080/api/quotes
```

### Update Quote

```bash
curl -X PUT http://localhost:8080/api/quotes/1 \
  -H "Content-Type: application/json" \
  -d '{"text":"Updated quote","author":"Updated Author"}'
```

### Search Quotes

```bash
curl "http://localhost:8080/api/quotes/search?q=Kubernetes"
```

### Delete Quote

```bash
curl -X DELETE http://localhost:8080/api/quotes/1
```

## Complete API Reference

### Base URL

- **Local Development:** `http://localhost:8080`
- **Production (NAIS):** Your ingress URL from `.nais/app.yaml`

### Endpoints

#### 1. Get All Quotes

```http
GET /api/quotes
```

**Response:** `200 OK`

```json
[
  {
    "id": "1",
    "text": "Deploy with confidence—let Nais handle the platform, you focus on the code.",
    "author": "Nais Team"
  }
]
```

#### 2. Get Quote by ID

```http
GET /api/quotes/{id}
```

**Path Parameters:**

- `id` (integer, required) - The quote ID

**Response:** `200 OK` or `404 Not Found`

#### 3. Create Quote

```http
POST /api/quotes
Content-Type: application/json
```

**Request Body:**

```json
{
  "text": "Your quote text here",
  "author": "Author Name"
}
```

**Response:** `201 Created`

```json
{
  "id": "6",
  "text": "Your quote text here",
  "author": "Author Name"
}
```

#### 4. Update Quote

```http
PUT /api/quotes/{id}
Content-Type: application/json
```

**Request Body:**

```json
{
  "text": "Updated quote text",
  "author": "Updated Author"
}
```

**Response:** `200 OK` or `404 Not Found`

#### 5. Delete Quote

```http
DELETE /api/quotes/{id}
```

**Response:** `204 No Content` or `404 Not Found`

#### 6. Search Quotes

```http
GET /api/quotes/search?q={query}
```

**Query Parameters:**

- `q` (string, required) - Search query (searches both text and author fields)

**Response:** `200 OK`

### Error Responses

All errors follow a consistent format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error description"
}
```

**Common Error Codes:**

- `BAD_REQUEST` - Invalid request (missing fields, invalid format)
- `NOT_FOUND` - Resource doesn't exist
- `MISSING_ID` - ID parameter is missing
- `INVALID_ID` - ID must be a number
- `MISSING_QUERY` - Search query parameter is missing
- `DATABASE_ERROR` - Internal database error
- `DESERIALIZATION_ERROR` - JSON parsing error

### Data Model

#### Quote (Response)

```json
{
  "id": "string",
  "text": "string (max 1000 chars)",
  "author": "string (max 255 chars)"
}
```

#### QuoteInput (Request for POST/PUT)

```json
{
  "text": "string (required, max 1000 chars)",
  "author": "string (required, max 255 chars)"
}
```

## Management Operations

### Bulk Import

Import multiple quotes from a JSON file:

```bash
# Create quotes.json with one quote per line
cat > quotes.json <<EOF
{"text":"Quote 1","author":"Author 1"}
{"text":"Quote 2","author":"Author 2"}
{"text":"Quote 3","author":"Author 3"}
EOF

# Import
while IFS= read -r line; do
  curl -X POST http://localhost:8080/api/quotes \
    -H "Content-Type: application/json" \
    -d "$line"
  sleep 0.1
done < quotes.json
```

### Export Quotes

```bash
# Export all quotes to JSON file
curl -s http://localhost:8080/api/quotes > quotes-backup.json

# Pretty print with jq
curl -s http://localhost:8080/api/quotes | jq '.' > quotes-backup.json
```

### Database Management

#### Direct SQL Access

```bash
# Local database
mise run db:psql

# Production (via NAIS)
nais postgres proxy quotes-backend
psql -h localhost -p 5432 -U <username> -d quotes
```

#### Common SQL Operations

```sql
-- View all quotes
SELECT * FROM quotes;

-- Add quote directly
INSERT INTO quotes (text, author) VALUES ('Direct insert', 'DBA');

-- Update quote
UPDATE quotes SET text = 'Updated text' WHERE id = 1;

-- Delete quote
DELETE FROM quotes WHERE id = 1;

-- Count quotes
SELECT COUNT(*) FROM quotes;

-- Search quotes
SELECT * FROM quotes WHERE text ILIKE '%search%' OR author ILIKE '%search%';
```

### Useful Scripts

#### Reset and Seed Database

```bash
#!/bin/bash
# reset-quotes.sh

# Delete all quotes
for id in $(curl -s http://localhost:8080/api/quotes | jq -r '.[].id'); do
  curl -X DELETE "http://localhost:8080/api/quotes/$id"
done

# Add new quotes
curl -X POST http://localhost:8080/api/quotes \
  -H "Content-Type: application/json" \
  -d '{"text":"First quote","author":"Author 1"}'

curl -X POST http://localhost:8080/api/quotes \
  -H "Content-Type: application/json" \
  -d '{"text":"Second quote","author":"Author 2"}'
```

#### Query Examples with jq

```bash
# Pretty print all quotes
curl -s http://localhost:8080/api/quotes | jq '.'

# Get only quote texts
curl -s http://localhost:8080/api/quotes | jq -r '.[].text'

# Count quotes
curl -s http://localhost:8080/api/quotes | jq 'length'

# Find quotes by author
curl -s http://localhost:8080/api/quotes | jq '.[] | select(.author == "Nais Team")'
```

### Production Management

#### Production Access

```bash
# Get pod name
kubectl get pods -n examples

# Port forward to access API
kubectl port-forward -n examples <pod-name> 8080:8080

# Then use local commands
curl http://localhost:8080/api/quotes
```

#### Monitoring Quote Operations

```bash
# Check quote count in real-time
watch -n 5 'curl -s http://localhost:8080/internal/stats | jq ".total_quotes"'

# View logs for quote operations
kubectl logs -n examples <pod-name> | grep -E "created|updated|deleted"
```

## Database

### Local Development

```bash
# Start database
mise run db:start

# Connect to database
mise run db:psql

# View database logs
mise run db:logs

# Reset database (destroys all data)
mise run db:reset
```

**Configuration:**

- Host: `localhost:5433`
- Database: `quotes`
- Username: `quotes`
- Password: `quotes`

### Production (NAIS)

Database is automatically provisioned via `.nais/app.yaml`:

- PostgreSQL 16 Cloud SQL instance
- Automatic connection injection via environment variables
- Audit logging enabled with pgAudit

### Database Schema

The application uses Exposed ORM for database operations. The schema is automatically created on application startup:

#### Table: quotes

- `id` - INTEGER (auto-increment, primary key)
- `text` - VARCHAR(1000) - Quote text
- `author` - VARCHAR(255) - Quote author

### Default Data

The application initializes with 5 default quotes on first startup. These are automatically inserted if the database is empty.

## Testing

```bash
# Run all tests
mise run test

# Run specific test suite
./gradlew test --tests "DatabaseTest"
./gradlew test --tests "ApplicationIntegrationTest"

# Run with coverage
./gradlew test jacocoTestReport
```

**Test Coverage:**

- ✅ 15 database tests (CRUD operations)
- ✅ 9 integration tests (HTTP endpoints)
- ✅ H2 in-memory database for fast testing
- ✅ No external dependencies required

### Test Structure

#### Unit Tests (DatabaseTest.kt)

Tests the database service layer in isolation:

- **testCreateQuote** - Validates quote creation
- **testGetAllQuotes** - Verifies retrieving all quotes
- **testGetQuoteById** - Tests fetching specific quote by ID
- **testGetQuoteByIdNotFound** - Confirms proper handling of non-existent quotes
- **testInitializeDefaultQuotes** - Validates default data initialization
- **testInitializeDefaultQuotesIdempotent** - Ensures initialization runs only once
- **testCreateMultipleQuotes** - Tests creating multiple quotes with unique IDs
- **testQuoteTextAndAuthorPreservation** - Verifies data integrity with long text
- **testEmptyDatabase** - Tests behavior with no data

#### Integration Tests (ApplicationIntegrationTest.kt)

Tests the full application stack with database:

- **testGetQuotesWithDatabase** - Validates GET /api/quotes endpoint
- **testCreateAndRetrieveQuote** - Tests POST then GET workflow
- **testGetAllQuotesIncludesNewlyCreated** - Verifies persistence across requests
- **testGetQuoteNotFoundReturns404** - Tests 404 error handling
- **testGetQuoteWithInvalidIdFormat** - Validates input validation
- **testCreateQuoteWithMissingFields** - Tests error handling for invalid data
- **testHealthEndpoint** - Validates health check endpoint
- **testReadinessEndpoint** - Tests readiness probe with DB initialization
- **testCreateMultipleQuotesSequentially** - Tests concurrent quote creation

### Test Database Configuration

Tests use **H2 in-memory database** with PostgreSQL compatibility mode:

```kotlin
jdbc:h2:mem:test_<timestamp>;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
```

**Benefits of H2 for Testing:**

- **Fast** - In-memory, no disk I/O
- **Isolated** - Each test gets a fresh database
- **No Setup** - No external database required
- **PostgreSQL Compatible** - Mimics production behavior

## Observability

### OpenTelemetry

Automatic instrumentation enabled for:

- **Traces**: Distributed tracing across services
- **Metrics**: Request rates, latencies, errors
- **Logs**: Structured JSON logs with correlation IDs

### Monitoring

```bash
# View logs
kubectl logs -n examples <pod-name>

# Check metrics
curl http://localhost:8080/internal/stats

# Access Grafana (local)
open http://localhost:4300
```

## Development

### Build

```bash
./gradlew build
```

### Run in Development Mode

```bash
# With auto-reload
./gradlew run --continuous

# Or using mise
mise run dev
```

### Code Quality

```bash
# Lint
mise run lint

# Format
./gradlew ktlintFormat

# Dependency updates
./gradlew dependencyUpdates
```

## Deployment

### NAIS Platform

The application is deployed via NAIS with configuration in `.nais/app.yaml`:

```bash
# Deploy to dev
nais deploy --cluster=dev-gcp --var ingress=https://quotes-backend.dev.nav.cloud.nais.io

# View deployment
kubectl get pods -n examples
```

### Environment Variables

**Local (auto-configured):**

- `DB_URL=jdbc:postgresql://localhost:5433/quotes`
- `DB_USERNAME=quotes`
- `DB_PASSWORD=quotes`

**Production (injected by NAIS):**

- `DB_URL` - Cloud SQL connection string
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

## Architecture

```text
┌─────────────────┐
│  quotes-frontend │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐      ┌──────────────┐
│ quotes-backend  │─────►│ PostgreSQL   │
│  (This Service) │      │  Cloud SQL   │
└────────┬────────┘      └──────────────┘
         │
         ▼
┌─────────────────┐
│  OpenTelemetry  │
│   Collector     │
└─────────────────┘
```

## Tech Stack

- **Framework**: Ktor 3.3.3
- **Language**: Kotlin 2.2.21
- **Database**: PostgreSQL 16 (Exposed ORM)
- **Observability**: OpenTelemetry
- **Testing**: JUnit, H2 Database
- **Build**: Gradle 8.14

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests: `mise run test`
4. Run linting: `mise run lint`
5. Submit a pull request

## License

This project is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details.
