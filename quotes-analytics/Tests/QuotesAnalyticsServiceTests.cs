using System.Net;
using System.Text.Json;
using FluentAssertions;
using Microsoft.Extensions.Logging;
using Moq;
using Nais.QuotesAnalytics.Models;
using Nais.QuotesAnalytics.Services;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

public class QuotesAnalyticsServiceTests
{
    private static QuotesAnalyticsService CreateService(string quotesJson)
    {
        var handler = new TestHttpMessageHandler(quotesJson);
        var httpClient = new HttpClient(handler) { BaseAddress = new Uri("http://test-backend") };
        var logger = Mock.Of<ILogger<QuotesAnalyticsService>>();
        return new QuotesAnalyticsService(httpClient, logger);
    }

    [Fact]
    public async Task GetAllAnalyticsAsync_ReturnsAnalyticsForAllQuotes()
    {
        var quotes = new[]
        {
            new { Id = "1", Text = "Hello world", Author = "Author" },
            new { Id = "2", Text = "Another quote here", Author = "Someone" },
        };
        var service = CreateService(JsonSerializer.Serialize(quotes));

        var result = await service.GetAllAnalyticsAsync();

        result.Should().HaveCount(2);
        result[0].QuoteId.Should().Be("1");
        result[1].QuoteId.Should().Be("2");
    }

    [Fact]
    public async Task GetAllAnalyticsAsync_ReturnsEmptyForNoQuotes()
    {
        var service = CreateService("[]");

        var result = await service.GetAllAnalyticsAsync();

        result.Should().BeEmpty();
    }

    [Fact]
    public async Task GetAnalyticsForQuoteAsync_ReturnsAnalytics()
    {
        var quote = new { Id = "42", Text = "Deploy with confidence", Author = "Nais Team" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("42");

        result.QuoteId.Should().Be("42");
        result.Text.Should().Be("Deploy with confidence");
        result.Author.Should().Be("Nais Team");
        result.WordCount.Should().Be(3);
        result.CharacterCount.Should().Be("Deploy with confidence".Length);
    }

    [Fact]
    public async Task GetAnalyticsForQuoteAsync_UsesCacheOnSecondCall()
    {
        var quote = new { Id = "1", Text = "Test quote", Author = "Author" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var first = await service.GetAnalyticsForQuoteAsync("1");
        var second = await service.GetAnalyticsForQuoteAsync("1");

        second.QuoteId.Should().Be(first.QuoteId);
        second.Text.Should().Be(first.Text);
    }

    [Fact]
    public async Task AnalyzeQuote_CountsWordsCorrectly()
    {
        var quote = new { Id = "1", Text = "one two three four five", Author = "A" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.WordCount.Should().Be(5);
    }

    [Fact]
    public async Task AnalyzeQuote_CountsCharactersCorrectly()
    {
        var text = "Hello World";
        var quote = new { Id = "1", Text = text, Author = "A" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.CharacterCount.Should().Be(text.Length);
    }

    [Fact]
    public async Task AnalyzeQuote_SentimentScoreInRange()
    {
        var quote = new { Id = "1", Text = "This is a great and amazing quote", Author = "A" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.SentimentScore.Should().BeInRange(-1.0, 1.0);
    }

    [Fact]
    public async Task AnalyzeQuote_CategorizesDeployAsPlatform()
    {
        var quote = new { Id = "1", Text = "Deploy your app to production", Author = "A" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.Category.Should().Be("Platform");
    }

    [Fact]
    public async Task AnalyzeQuote_CategorizesSecurityQuote()
    {
        var quote = new { Id = "1", Text = "Secure by default is the way", Author = "A" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.Category.Should().Be("Security");
    }

    [Fact]
    public async Task AnalyzeQuote_CategorizesDevOpsQuote()
    {
        var quote = new { Id = "1", Text = "Continuous delivery is the goal", Author = "DevOps" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.Category.Should().Be("DevOps");
    }

    [Fact]
    public async Task AnalyzeQuote_DefaultsToGeneral()
    {
        var quote = new { Id = "1", Text = "Just a regular quote", Author = "Regular Person" };
        var service = CreateService(JsonSerializer.Serialize(quote));

        var result = await service.GetAnalyticsForQuoteAsync("1");

        result.Category.Should().Be("General");
    }

    [Fact]
    public async Task GetAnalyticsSummaryAsync_ReturnsSummary()
    {
        var quotes = new[]
        {
            new { Id = "1", Text = "Deploy with confidence", Author = "Nais Team" },
            new { Id = "2", Text = "Secure by default", Author = "Security Advocate" },
        };
        var service = CreateService(JsonSerializer.Serialize(quotes));

        var summary = await service.GetAnalyticsSummaryAsync();

        summary.TotalQuotes.Should().Be(2);
        summary.AverageWordCount.Should().BeGreaterThan(0);
        summary.CategoryDistribution.Should().NotBeEmpty();
        summary.MostCommonCategory.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task GetAnalyticsSummaryAsync_EmptyQuotes_ReturnsZeroSummary()
    {
        var service = CreateService("[]");

        var summary = await service.GetAnalyticsSummaryAsync();

        summary.TotalQuotes.Should().Be(0);
        summary.MostCommonCategory.Should().Be("N/A");
    }

    private class TestHttpMessageHandler : HttpMessageHandler
    {
        private readonly string _responseContent;

        public TestHttpMessageHandler(string responseContent)
        {
            _responseContent = responseContent;
        }

        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            return Task.FromResult(new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(_responseContent, System.Text.Encoding.UTF8, "application/json")
            });
        }
    }
}
