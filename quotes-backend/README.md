# Quotes Backend

The Quotes Backend is a Kotlin-based service built with Ktor. It provides a simple API for managing quotes, storing them in memory.

## Features

- **Endpoints**:
  - `GET /api/quotes`: Retrieves all quotes.
  - `POST /api/quotes`: Adds a new quote.
  - `GET /api/quotes/{id}`: Retrieves a quote by its ID.
- **In-Memory Storage**: Quotes are stored in a thread-safe `ConcurrentHashMap`.
- **OpenAPI Specification**: The API specification is available at `/openapi`.
- **Swagger UI**: Interactive API documentation is available at `/swagger`.

## Prerequisites

- [Kotlin](https://kotlinlang.org/) 1.8 or later
- [Gradle](https://gradle.org/) 7.0 or later

## Running the Service

1. Clone the repository:

   ```bash
   git clone https://github.com/nais/examples/quotes-backend.git
   cd quotes-backend
   ```

2. Build the project:

   ```bash
   ./gradlew build
   ```

3. Run the service:

   ```bash
   ./gradlew run
   ```

   The service will start on `http://localhost:8080`.

## API Endpoints

### `GET /api/quotes`

Retrieves all quotes.

#### Response

- **200 OK**: Returns a list of quotes.

### `POST /api/quotes`

Adds a new quote.

#### Request Body

- `id` (string): Unique identifier for the quote.
- `text` (string): The quote text.
- `author` (string): The author of the quote.

#### Response

- **200 OK**: Returns the created quote.

### `GET /api/quotes/{id}`

Retrieves a quote by its ID.

#### Path Parameters

- `id` (string): The ID of the quote to retrieve.

#### Response

- **200 OK**: Returns the quote.
- **404 Not Found**: If the quote does not exist.

## OpenAPI and Swagger

- OpenAPI specification: [http://localhost:8080/openapi](http://localhost:8080/openapi)
- Swagger UI: [http://localhost:8080/swagger](http://localhost:8080/swagger)

## Testing

Run the tests using Gradle:

```bash
./gradlew test
```

## License

This project is licensed under the MIT License. See the LICENSE file for details.
