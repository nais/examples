# Spring Boot with PostgreSQL

This is a simple example application that demonstrates how to use Kotlin, Spring Boot, and PostgreSQL together. The application provides a REST API for managing heroes, which are stored in a PostgreSQL database.

## Prerequisites

Before you can run this application, you will need to have the following installed:

* Java 17 or later
* Gradle 8.4 or later
* PostgreSQL 13 or later (via Docker)

## Getting Started

To get started, follow these steps:

1. Clone the repository:

    ```shell
    git clone https://github.com/nais/examples.git
    ```

1. Navigate to the project directory:

    ```shell
    cd db-dings
    ```

1. Start the PostgreSQL database:

    ```shell
    docker-compose up db -d
    ```

1. Build the application:

    ```shell
    ./gradlew build
    ```

1. Run the application:

    ```shell
    ./gradlew bootRun
    ```

This will start the application on port 8080 on localhost.

Test the API using a tool like `curl` or `httpie`:

```shell
http localhost:8080/api/heroes
```

This should return an empty JSON array.

## API Documentation

The API provides the following endpoints:

* `GET /api/heroes`: Returns a list of all heroes.
* `GET /api/heroes/{id}`: Returns the hero with the specified ID.
* `POST /api/heroes`: Creates a new hero.
* `PUT /api/heroes/{id}`: Updates the hero with the specified ID.
* `DELETE /api/heroes/{id}`: Deletes the hero with the specified ID.

The request and response bodies are in JSON format. Here's an example of a hero object:

```json
{
  "firstName": "Luke",
  "lastName": "Skywalker",
  "species": "HUMAN"
}
```

The species field can be one of the following values: `HUMAN`, `WOOKIEE`, or `YODA_SPECIES`.

## License

This project is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details.
