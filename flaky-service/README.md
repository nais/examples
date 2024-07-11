# Flaky Service

The Flaky Service is a http simple service that aims to simulate flaky behavior in a service oriented architecture.

## Architecture

The flaky service is written in Go and listens on port `8080` by default. It has a single endpoint that returns a `200 OK` response for good requests and a `500 Server Error` response for errors.

The flakiness is simulated by returning a `500 Server Error` response for a percentage of requests. The flakiness limit can be adjusted using the Unleash feature toggle mentioned below.

## Endpoint

The service has only one endpoint:

### `/`

- Returns a `200 OK {"message": "hello, world"}` response for good requests.
- Returns a `500 Server Error {"error": "server error"}` response for errors.

## Local Development

Start Unleash:

```shell
docker-compose -f ../docker-compose.yml up unleash -d
```

The Unleash server URL is `http://localhost:4242` and the default user is `admin` with the password `unleash4all`.

Start the flaky service:

```shell
make flaky-service
```

### Adjusting Flakiness

The flakiness of the service can be adjusted using the Unleash feature toggle. The service checks the `flaky-service.flakiness-level` feature toggle variant to determine the flakiness limit. If the feature toggle is not pressent, the service defaults to a flakiness limit of `50%`.

To create the feature toggle, you can use the Unleash UI or the Unleash API. The following is an example of how to create the feature toggle using the Unleash API:

```shell
export UNLEASH_API_KEY="*:*.admin-token"
export UNLEASH_SERVER_URL="http://localhost:4242"
```

```shell
curl --location --request POST "$UNLEASH_SERVER_URL/api/admin/projects/default/features" \
    --header "Authorization: $UNLEASH_API_KEY" \
    --header "Content-Type: application/json" \
    --data-raw "{
      "type": "permission",
      "name": "flaky-service.flakiness-level",
      "description": "",
      "impressionData": false
    }"
```

Enable the feature toggle for the `development` environment:

```shell
curl --location --request POST "$UNLEASH_SERVER_URL/api/admin/projects/default/features/flaky-service.flakiness-level/environments/development/on" \
    --header "Authorization: $UNLEASH_API_KEY" \
    --header "Content-Type: application/json"
```

Get the strategy id:

```shell
export STRATEGY_ID=$(curl --location "$UNLEASH_SERVER_URL/api/admin/projects/default/features/flaky-service.flakiness-level/environments/development/strategies" \
    --header "Authorization: $UNLEASH_API_KEY" | jq ".[0].id")
```

Set the flakiness level as variant payload:

```shell
curl --location --request PUT "$UNLEASH_SERVER_URL/api/admin/projects/default/features/flaky-service.flakiness-level/environments/development/strategies/$STRATEGY_ID" \
    --header "Authorization: $UNLEASH_API_KEY" \
    --header "Content-Type: application/json" \
    --data-raw "{
  "name": "flexibleRollout",
  "title": "",
  "constraints": [],
  "parameters": {
    "rollout": "100",
    "stickiness": "default",
    "groupId": "flaky-service.flakiness-level"
  },
  "variants": [
    {
      "stickiness": "default",
      "name": "flakiness-level",
      "weight": 1000,
      "payload": {
        "type": "number",
        "value": "69"
      },
      "weightType": "variable"
    }
  ],
  "segments": [],
  "disabled": false
}"
```
