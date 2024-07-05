# Flaky Service

The Flaky Service is a simple service that aims to simulate flaky behavior in a service oriented architecture.

## Architecture

The service is a simple HTTP server that listens on port `8080` by default written in Go. It has a single endpoint that returns a `200 OK` response for good requests and a `500 Server Error` response for errors.

The flakiness is simulated by randomly returning a `500 Server Error` response for a percentage of requests. The flakiness limit can be adjusted using the Unleash feature toggle mentioned below.

## Endpoint

The service has only one endpoint:

### `/`

- Returns a `200 OK {"message": "hello, world"}` response for good requests.
- Returns a `500 Server Error {"error": "server error"}` response for errors.

## Adjusting Flakiness

The flakiness of the service can be adjusted using the Unleash feature toggle. The service checks the `flaky-service.flakiness-limit` feature toggle to determine the flakiness limit. If the feature toggle is not present, the service defaults to a flakiness limit of `50%`.
