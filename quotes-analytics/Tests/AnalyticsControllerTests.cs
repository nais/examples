using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Nais.QuotesAnalytics.Models;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

public class AnalyticsControllerTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    private static readonly string QuotesJson = JsonSerializer.Serialize(new[]
    {
        new { Id = "1", Text = "Deploy with confidence", Author = "Nais Team" },
        new { Id = "2", Text = "Kubernetes is complex but Nais makes it simple", Author = "Platform Engineer" },
    });

    public AnalyticsControllerTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder =>
        {
            builder.ConfigureServices(services =>
            {
                services.AddHttpClient<Nais.QuotesAnalytics.Services.QuotesAnalyticsService>(client =>
                {
                    client.BaseAddress = new Uri("http://fake-backend");
                })
                .ConfigurePrimaryHttpMessageHandler(() => new MockHttpMessageHandler(QuotesJson));
            });
        });
    }

    [Fact]
    public async Task GetAnalyticsSummary_ReturnsOk()
    {
        var client = _factory.CreateClient();

        var response = await client.GetAsync("/api/analytics/summary");

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var content = await response.Content.ReadAsStringAsync();
        content.Should().Contain("totalQuotes");
    }

    [Fact]
    public async Task GetAllAnalytics_ReturnsOkWithResults()
    {
        var client = _factory.CreateClient();

        var response = await client.GetAsync("/api/analytics");

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var analytics = await response.Content.ReadFromJsonAsync<List<QuoteAnalytics>>();
        analytics.Should().NotBeNull();
        analytics!.Count.Should().Be(2);
    }
}

internal class MockHttpMessageHandler : HttpMessageHandler
{
    private readonly string _responseContent;

    public MockHttpMessageHandler(string responseContent)
    {
        _responseContent = responseContent;
    }

    protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        var path = request.RequestUri?.PathAndQuery ?? "";

        string content;
        if (path.StartsWith("/api/quotes/"))
        {
            var id = path.Split('/').Last();
            content = JsonSerializer.Serialize(new { Id = id, Text = "Deploy with confidence", Author = "Nais Team" });
        }
        else
        {
            content = _responseContent;
        }

        return Task.FromResult(new HttpResponseMessage(HttpStatusCode.OK)
        {
            Content = new StringContent(content, System.Text.Encoding.UTF8, "application/json")
        });
    }
}
