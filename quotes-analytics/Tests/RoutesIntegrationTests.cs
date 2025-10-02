using System.Net;
using System.Text.Json;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

[Collection("Integration")]
public class RoutesIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;
    private readonly HttpClient _client;

    public RoutesIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
        _client = _factory.CreateClient();
    }

    [Fact]
    public async Task AnalyticsRoutes_HaveCorrectPaths()
    {
        // Test analytics endpoints exist (not 404) and handle backend unavailability gracefully
        var routes = new[]
        {
            "/api/analytics",
            "/api/analytics/summary"
        };

        foreach (var route in routes)
        {
            var response = await _client.GetAsync(route);

            // Should not be 404 (routes exist), but may fail due to backend unavailability
            response.StatusCode.Should().NotBe(HttpStatusCode.NotFound,
                $"Route {route} should exist");

            // Should be either 200 (success) or 500/503 (backend unavailable)
            var validStatusCodes = new[] { HttpStatusCode.OK, HttpStatusCode.InternalServerError, HttpStatusCode.ServiceUnavailable };
            validStatusCodes.Should().Contain(response.StatusCode,
                $"Route {route} should return valid status code but returned {response.StatusCode}");
        }
    }

    [Fact]
    public async Task HealthEndpoints_ReturnCorrectFormat()
    {
        var healthResponse = await _client.GetAsync("/internal/health");
        var readyResponse = await _client.GetAsync("/internal/ready");

        healthResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        readyResponse.StatusCode.Should().Be(HttpStatusCode.OK);

        var healthContent = await healthResponse.Content.ReadAsStringAsync();
        var readyContent = await readyResponse.Content.ReadAsStringAsync();

        // Verify JSON format
        healthContent.Should().Contain("status");
        healthContent.Should().Contain("healthy");
        healthContent.Should().Contain("quotes-analytics");

        readyContent.Should().Contain("status");
        readyContent.Should().Contain("ready");
        readyContent.Should().Contain("quotes-analytics");
    }

    [Fact]
    public async Task AnalyticsRoutes_ExistInRouting()
    {
        // Test that routes are properly registered (should not return 404)
        // This tests routing configuration independent of backend availability
        var routes = new[]
        {
            "/api/analytics/test-quote-id"
        };

        foreach (var route in routes)
        {
            var response = await _client.GetAsync(route);

            // Route should exist (not 404), but may return error due to backend unavailability
            // 404 means the route isn't configured, other errors mean the route exists but fails
            response.StatusCode.Should().NotBe(HttpStatusCode.NotFound,
                $"Route {route} should be configured in the controller");
        }
    }

    [Fact]
    public async Task AnalyticsEndpoints_ReturnJsonContentType()
    {
        var routes = new[]
        {
            "/api/analytics",
            "/api/analytics/summary"
        };

        foreach (var route in routes)
        {
            var response = await _client.GetAsync(route);

            if (response.IsSuccessStatusCode)
            {
                response.Content.Headers.ContentType?.MediaType
                    .Should().Be("application/json", $"Route {route} should return JSON");
            }
        }
    }

    [Fact]
    public async Task InvalidQuoteId_ReturnsAppropriateError()
    {
        // Test with a trailing slash - this should route to GetAllAnalytics and return 200
        var trailingSlashResponse = await _client.GetAsync("/api/analytics/");
        // This routes to the GetAllAnalytics endpoint, which should return 200 or service unavailable
        var validTrailingSlashResponses = new[] { HttpStatusCode.OK, HttpStatusCode.ServiceUnavailable };
        validTrailingSlashResponses.Should().Contain(trailingSlashResponse.StatusCode,
            "/api/analytics/ should route to GetAllAnalytics endpoint");

        // Test with a non-existent quote ID (should handle gracefully)
        var nonExistentResponse = await _client.GetAsync("/api/analytics/non-existent-quote");

        // Should be either 404 (quote not found), 500 (backend error), or 503 (service unavailable)
        var validErrorCodes = new[] { HttpStatusCode.NotFound, HttpStatusCode.BadRequest, HttpStatusCode.InternalServerError, HttpStatusCode.ServiceUnavailable };
        validErrorCodes.Should().Contain(nonExistentResponse.StatusCode,
            "Non-existent quote should return appropriate error");
    }
}
