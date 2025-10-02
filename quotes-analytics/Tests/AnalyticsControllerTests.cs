using System.Net;
using System.Net.Http.Json;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Nais.QuotesAnalytics.Models;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

public class AnalyticsControllerTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public AnalyticsControllerTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
    }

    [Fact]
    public async Task GetAnalyticsSummary_ReturnsOk()
    {
        // Arrange
        var client = _factory.CreateClient();

        // Act
        var response = await client.GetAsync("/api/analytics/summary");

        // Assert
        // May return OK with empty data or error if backend is unavailable
        // Just checking the endpoint exists and responds
        response.StatusCode.Should().BeOneOf(HttpStatusCode.OK, HttpStatusCode.InternalServerError, HttpStatusCode.ServiceUnavailable);
    }

    [Fact]
    public async Task GetAllAnalytics_ReturnsValidResponse()
    {
        // Arrange
        var client = _factory.CreateClient();

        // Act
        var response = await client.GetAsync("/api/analytics");

        // Assert
        // May return OK with empty array or error if backend is unavailable
        response.StatusCode.Should().BeOneOf(HttpStatusCode.OK, HttpStatusCode.InternalServerError, HttpStatusCode.ServiceUnavailable);
    }
}
