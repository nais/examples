package com.example

import io.ktor.client.call.body // Added import for response.body<Type>()
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation // Alias for client plugin
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.* // For client-side JSON serialization
import io.ktor.server.application.* // Import Application from Ktor
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull // Added for assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

// kotlinx.serialization.serializer and builtins.serializer are often not needed with direct type
// usage

class ApplicationTest {

        @Test
        fun testGetQuotes() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application {
                        module() // Call module within application block
                }
                val client = createClient {}
                val response = client.get("/api/quotes")
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBodyString = response.bodyAsText()

                val jsonElement =
                        kotlinx.serialization.json.Json.parseToJsonElement(responseBodyString)

                kotlin.test.assertTrue(
                        jsonElement is kotlinx.serialization.json.JsonArray,
                        "Expected response body to be a JSON array, but it was: ${jsonElement::class.simpleName}"
                )

                val expectedSize = 5 // Update this to match the number of quotes in your data
                kotlin.test.assertEquals(
                        expectedSize,
                        jsonElement.size,
                        "Expected JSON array size to be $expectedSize, but it was: ${jsonElement.size}"
                )
        }

        @Test
        fun testGetSpecificQuote() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application {
                        module() // Call module within application block
                }
                val client = createClient {}

                // Assuming a quote with ID 1 exists in the database
                val response = client.get("/api/quotes/1")
                assertEquals(HttpStatusCode.OK, response.status)

                val responseBodyString = response.bodyAsText()

                val json = kotlinx.serialization.json.Json
                val actualJsonElement = json.parseToJsonElement(responseBodyString)

                val actualJsonObject =
                        actualJsonElement as? kotlinx.serialization.json.JsonObject
                                ?: error(
                                        "Expected JsonElement to be a JsonObject, but it was: ${actualJsonElement::class.simpleName}"
                                )

                kotlin.test.assertTrue(
                        actualJsonObject.containsKey("id"),
                        "Response JSON should contain an 'id' field"
                )
                kotlin.test.assertEquals(
                        1,
                        actualJsonObject["id"]?.jsonPrimitive?.content?.toInt()
                                ?: error("Missing 'id' field"),
                        "ID does not match"
                )
                kotlin.test.assertTrue(
                        actualJsonObject.containsKey("text"),
                        "Response JSON should contain a 'text' field"
                )
                kotlin.test.assertTrue(
                        actualJsonObject.containsKey("author"),
                        "Response JSON should contain an 'author' field"
                )
        }

        @Test
        fun testPostQuote() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application {
                        module() // Call module within application block
                }
                val client = createClient {}
                val quoteJsonString = """{"text":"Test Quote","author":"Author"}"""
                val postResponse =
                        client.post("/api/quotes") {
                                contentType(ContentType.Application.Json)
                                setBody(quoteJsonString)
                        }
                assertEquals(HttpStatusCode.Created, postResponse.status)

                val retrievedQuoteString = postResponse.bodyAsText()

                val json = kotlinx.serialization.json.Json
                val actualJsonElement = json.parseToJsonElement(retrievedQuoteString)

                val actualJsonObject =
                        actualJsonElement as? kotlinx.serialization.json.JsonObject
                                ?: error(
                                        "Expected JsonElement to be a JsonObject, but it was: ${actualJsonElement::class.simpleName}"
                                )
                kotlin.test.assertTrue(
                        actualJsonObject.containsKey("id"),
                        "Response JSON should contain an 'id' field"
                )
                kotlin.test.assertEquals(
                        "Test Quote",
                        actualJsonObject["text"]?.jsonPrimitive?.content
                                ?: error("Missing 'text' field"),
                        "Text does not match"
                )
                kotlin.test.assertEquals(
                        "Author",
                        actualJsonObject["author"]?.jsonPrimitive?.content
                                ?: error("Missing 'author' field"),
                        "Author does not match"
                )

                val newQuoteId =
                        actualJsonObject["id"]?.jsonPrimitive?.content?.toInt()
                                ?: error("Missing 'id' field in the response")

                val getResponse = client.get("/api/quotes/$newQuoteId")
                assertEquals(HttpStatusCode.OK, getResponse.status)

                val retrievedQuoteBody = getResponse.bodyAsText()
                val retrievedJsonElement = json.parseToJsonElement(retrievedQuoteBody)

                val retrievedJsonObject =
                        retrievedJsonElement as? kotlinx.serialization.json.JsonObject
                                ?: error(
                                        "Expected JsonElement to be a JsonObject, but it was: ${retrievedJsonElement::class.simpleName}"
                                )

                kotlin.test.assertEquals(
                        newQuoteId,
                        retrievedJsonObject["id"]?.jsonPrimitive?.content?.toInt()
                                ?: error("Missing 'id' field"),
                        "Retrieved quote ID does not match"
                )
                kotlin.test.assertEquals(
                        "Test Quote",
                        retrievedJsonObject["text"]?.jsonPrimitive?.content
                                ?: error("Missing 'text' field"),
                        "Retrieved quote text does not match"
                )
                kotlin.test.assertEquals(
                        "Author",
                        retrievedJsonObject["author"]?.jsonPrimitive?.content
                                ?: error("Missing 'author' field"),
                        "Retrieved quote author does not match"
                )
        }

        @Test
        fun testPostQuoteWithMarshalling() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application { module() } // Call module within application block

                // Create a test client with ContentNegotiation for the client
                val client = createClient {
                        // Install client-side ContentNegotiation for JSON
                        install(ClientContentNegotiation) {
                                json(
                                        Json {
                                                prettyPrint = true
                                                isLenient = true
                                                ignoreUnknownKeys =
                                                        true // Good practice for robustness
                                        }
                                )
                        }
                }
                // Ensure your Quote class is @Serializable
                val quoteRequest = Quote(text = "Test Quote", author = "Author")

                val postResponse =
                        client.post("/api/quotes") {
                                contentType(ContentType.Application.Json)
                                // Ktor client can automatically serialize 'quoteRequest' if
                                // ContentNegotiation is installed
                                setBody(quoteRequest)
                        }
                assertEquals(HttpStatusCode.Created, postResponse.status)

                // Ktor client can automatically deserialize to 'Quote' if
                // ContentNegotiation is installed
                val createdQuote: Quote = postResponse.body()

                assertEquals("Test Quote", createdQuote.text, "Text does not match")
                assertEquals("Author", createdQuote.author, "Author does not match")
                assertNotNull(createdQuote.id, "ID should not be null")

                // Fetch the created quote by ID
                val getResponse = client.get("/api/quotes/${createdQuote.id}")
                assertEquals(HttpStatusCode.OK, getResponse.status)

                val retrievedQuoteById: Quote = getResponse.body()

                assertEquals(
                        createdQuote.id,
                        retrievedQuoteById.id,
                        "Retrieved quote ID does not match"
                )
                assertEquals(
                        createdQuote.text,
                        retrievedQuoteById.text,
                        "Retrieved quote text does not match"
                )
                assertEquals(
                        createdQuote.author,
                        retrievedQuoteById.author,
                        "Retrieved quote author does not match"
                )
        }

        @Test
        fun testGetQuoteNotFound() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application {
                        module() // Call module within application block
                }
                val client = createClient {}

                // Attempt to retrieve a quote with an ID that doesn't exist
                val response = client.get("/api/quotes/9999")
                assertEquals(HttpStatusCode.NotFound, response.status)

                val responseBodyString = response.bodyAsText()
                val json = kotlinx.serialization.json.Json
                val jsonElement = json.parseToJsonElement(responseBodyString)

                // Check that the error response is a JSON object and contains the expected fields
                val jsonObject =
                        jsonElement as? kotlinx.serialization.json.JsonObject
                                ?: error("Expected JSON object for error response")

                kotlin.test.assertTrue(
                        jsonObject.containsKey("error"),
                        "Error response should contain an 'error' field"
                )
                kotlin.test.assertTrue(
                        jsonObject.containsKey("message"),
                        "Error response should contain a 'message' field"
                )
        }

        @Test
        fun testPostQuoteInvalidFormat() = testApplication {
                globalErrorRate = 0.0 // Set error rate to 0 for this test
                application {
                        module() // Call module within application block
                }
                val client = createClient {}
                val invalidQuoteJsonString = """{"text":"Missing Author"}""" // Missing author field
                val postResponse =
                        client.post("/api/quotes") {
                                contentType(ContentType.Application.Json)
                                setBody(invalidQuoteJsonString)
                        }
                assertEquals(HttpStatusCode.BadRequest, postResponse.status)

                // val responseBodyString = postResponse.bodyAsText()
                // val json = kotlinx.serialization.json.Json
                // val jsonElement = json.parseToJsonElement(responseBodyString)

                //// Check that the error response is a JSON object and contains the expected fields
                // val jsonObject =
                //        jsonElement as? kotlinx.serialization.json.JsonObject
                //                ?: error("Expected JSON object for error response")

                // kotlin.test.assertTrue(
                //        jsonObject.containsKey("error"),
                //        "Error response should contain an 'error' field"
                // )
                // kotlin.test.assertTrue(
                //        jsonObject.containsKey("message"),
                //        "Error response should contain a 'message' field"
                // )
        }

        @Test
        fun testErrorRateSimulation() = testApplication {
                // Don't set globalErrorRate to 0.0 here, as this test specifically tests the error
                // rate logic
                application { module() }
                val client = createClient {}

                // Test with errorRate = 1.0 (100% errors)
                globalErrorRate = 1.0
                var errorResponses = 0
                val totalRequests = 10
                for (i in 1..totalRequests) {
                        val response = client.get("/api/quotes")
                        if (response.status == HttpStatusCode.InternalServerError) {
                                errorResponses++
                        }
                }
                assertEquals(
                        totalRequests,
                        errorResponses,
                        "Expected all requests to fail with errorRate = 1.0"
                )

                // Test with errorRate = 0.0 (0% errors)
                globalErrorRate = 0.0
                errorResponses = 0
                for (i in 1..totalRequests) {
                        val response = client.get("/api/quotes")
                        if (response.status == HttpStatusCode.InternalServerError) {
                                errorResponses++
                        }
                }
                assertEquals(0, errorResponses, "Expected no requests to fail with errorRate = 0.0")
        }

        @Test
        fun testInternalHealthEndpoint() = testApplication {
                application { module() }
                val client = createClient {}
                val response = client.get("/internal/health")
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("Application is healthy", response.bodyAsText())
        }
}
