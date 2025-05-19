# Quotes Load Generator

The Quotes Load Generator is a simple Go-based tool designed to generate load on a set of URLs. It is useful for testing the performance and reliability of the Quotes Frontend application or other services.

## Features

- **Configurable Load**: Specify the URLs, requests per second, and duration of the load test.
- **Concurrency**: Generates concurrent requests to simulate real-world traffic.
- **Lightweight**: Built with Go for minimal resource usage.

## Prerequisites

- [Go](https://golang.org/) 1.20 or later
- Docker (optional, for containerized usage)

## Usage

### Folder Structure

- `cmd/`: Contains the CLI commands and the `main.go` entry point for the application.
- `internal/loadgen/`: Contains the core logic for generating load.

### Command-Line Options

- `--url`: List of URLs to load (can be specified multiple times).
- `--hostname`: Hostname to prefix to all URLs (optional, should not include protocol).
- `--protocol`: Protocol to use for URLs (`http` or `https`, default: `https`).
- `--rps`: Number of requests per second (default: `10`).
- `--duration`: Duration of the load test in seconds (default: `10`).
- `--metrics`: Enable Prometheus metrics endpoint. When enabled, metrics about the load test will be exposed at the `/metrics` endpoint.
- `--metrics-port`: Port for the Prometheus metrics endpoint (default: `8080`).

### Running Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/nais/examples/quotes-loadgen.git
   cd quotes-loadgen
   ```

2. Build the application:

   ```bash
   cd cmd
   go build -o quotes-loadgen
   ```

3. Run the load generator:

   ```bash
   ./quotes-loadgen load --url /api/quotes --url /api/healthz --hostname "localhost:3000" --protocol "http" --rps 20 --duration 15
   ```

   - This example generates 20 requests per second for 15 seconds on the URLs `/api/quotes` and `/api/healthz` with the hostname `localhost:3000` and the `http` protocol.

### Running with Docker

1. Build the Docker image:

   ```bash
   docker build -t quotes-loadgen .
   ```

2. Run the container:

   ```bash
   docker run --rm quotes-loadgen load --url /api/quotes --url /api/healthz --hostname "localhost:3000" --protocol "http" --rps 20 --duration 15
   ```

## Environment Variables

The Quotes Load Generator supports the following environment variables for configuration:

- `LOADGEN_URLS`: A comma-separated list of URLs to load. Overrides the `--url` flag.
- `LOADGEN_HOSTNAME`: Hostname to prefix to all URLs. Overrides the `--hostname` flag.
- `LOADGEN_PROTOCOL`: Protocol to use for URLs (`http` or `https`). Overrides the `--protocol` flag.
- `LOADGEN_RPS`: Number of requests per second. Overrides the `--rps` flag.
- `LOADGEN_DURATION`: Duration of the load test in seconds. Overrides the `--duration` flag.
- `LOADGEN_METRICS`: Set to `true` to enable the Prometheus metrics endpoint. Overrides the `--metrics` flag.
- `LOADGEN_METRICS_PORT`: Specifies the port for the Prometheus metrics endpoint. Overrides the `--metrics-port` flag (default: `8080`).

### Example Usage with Environment Variables

You can configure the load generator using environment variables instead of command-line flags:

```bash
export LOADGEN_URLS="/api/quotes,/api/healthz"
export LOADGEN_HOSTNAME="localhost:3000"
export LOADGEN_PROTOCOL="http"
export LOADGEN_RPS=20
export LOADGEN_DURATION=15

./quotes-loadgen load
```

## Deployment

The application can be deployed to the NAIS platform using the provided `.nais/config.yaml` file. Ensure the Docker image is pushed to a container registry accessible by NAIS.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
