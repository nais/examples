# Sentiment API

Simple REST API for analyzing the sentiment of text.

## Prerequisites

Before you can run this application, you will need to have the following installed:

* Java 17 or later
* Gradle 8.4 or later
* Google Cloud Account

## Getting Started

To get started, follow these steps:

1. Clone the repository:

    ```shell
    git clone https://github.com/nais/examples.git
    ```

1. Navigate to the project directory:

    ```shell
    cd shop-backend
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
http localhost:8080/api/products
```

This should return an empty JSON array.

## API Documentation

The API provides the following endpoints:

* `GET /api/products`: Returns a list of all products.
* `GET /api/products/{id}`: Returns the product with the specified ID.
* `POST /api/products`: Creates a new product.
* `PUT /api/products/{id}`: Updates the product with the specified ID.
* `DELETE /api/products/{id}`: Deletes the product with the specified ID.
* `GET /api/products/{id}/reviews`: Returns a list of all reviews for the product with the specified ID.
* `POST /api/products/{id}/reviews`: Creates a new review for the product with the specified ID.

The request and response bodies are in JSON format. Here's an example of a product object:

```json
{
    "name": "Lightsaber",
    "description": "An elegant weapon for a more civilized age.",
    "category": "OTHER",
    "price": 1000.00,
    "images": []
}
```

The species field can be one of the following values: `TEE_SHIRT`, `HOODIE`, `CAP`, `OTHER`.

## License

This project is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details.
