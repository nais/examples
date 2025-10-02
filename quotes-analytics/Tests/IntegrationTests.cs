using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

[Trait("Category", "Integration")]
public class IntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;

    public IntegrationTests(WebApplicationFactory<Program> factory)
    {
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task HealthEndpoint_ReturnsHealthyStatus()
    {
        var response = await _client.GetAsync("/internal/health");
        var content = await response.Content.ReadAsStringAsync();

        response.EnsureSuccessStatusCode();
        content.Should().Contain("healthy");
        content.Should().Contain("quotes-analytics");
    }

    [Fact]
    public async Task ReadyEndpoint_ReturnsReadyStatus()
    {
        var response = await _client.GetAsync("/internal/ready");
        var content = await response.Content.ReadAsStringAsync();

        response.EnsureSuccessStatusCode();
        content.Should().Contain("ready");
        content.Should().Contain("quotes-analytics");
    }

    [Fact]
    public async Task HealthEndpoint_ReturnsApplicationJson()
    {
        var response = await _client.GetAsync("/internal/health");

        response.Content.Headers.ContentType?.MediaType.Should().Be("application/json");
    }

    [Fact]
    public async Task AnalyticsEndpoints_AreReachable()
    {
        var summaryResponse = await _client.GetAsync("/api/analytics/summary");
        var allAnalyticsResponse = await _client.GetAsync("/api/analytics");

        summaryResponse.StatusCode.Should().NotBe(System.Net.HttpStatusCode.NotFound);
        allAnalyticsResponse.StatusCode.Should().NotBe(System.Net.HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task NonExistentEndpoint_Returns404()
    {
        var response = await _client.GetAsync("/api/nonexistent");

        response.StatusCode.Should().Be(System.Net.HttpStatusCode.NotFound);
    }
}
