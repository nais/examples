using FluentAssertions;
using Nais.QuotesAnalytics.Models;
using Xunit;

namespace Nais.QuotesAnalytics.Tests;

public class ModelsTests
{
    [Fact]
    public void QuoteAnalytics_ShouldCreateWithAllProperties()
    {
        // Arrange & Act
        var analytics = new QuoteAnalytics(
            QuoteId: "test-123",
            Text: "Test quote",
            Author: "Test Author",
            WordCount: 2,
            CharacterCount: 10,
            SentimentScore: 0.5,
            Category: "Test",
            AnalyzedAt: DateTime.UtcNow
        );

        // Assert
        analytics.QuoteId.Should().Be("test-123");
        analytics.Text.Should().Be("Test quote");
        analytics.Author.Should().Be("Test Author");
        analytics.WordCount.Should().Be(2);
        analytics.CharacterCount.Should().Be(10);
        analytics.SentimentScore.Should().Be(0.5);
        analytics.Category.Should().Be("Test");
        analytics.AnalyzedAt.Should().BeCloseTo(DateTime.UtcNow, TimeSpan.FromSeconds(5));
    }

    [Fact]
    public void AnalyticsSummary_ShouldCreateWithAllProperties()
    {
        // Arrange & Act
        var categoryDist = new Dictionary<string, int>
        {
            { "Platform", 5 },
            { "Security", 3 }
        };

        var summary = new AnalyticsSummary(
            TotalQuotes: 8,
            AverageWordCount: 15.5,
            AverageCharacterCount: 95.2,
            AverageSentimentScore: 0.3,
            CategoryDistribution: categoryDist,
            MostCommonCategory: "Platform"
        );

        // Assert
        summary.TotalQuotes.Should().Be(8);
        summary.AverageWordCount.Should().Be(15.5);
        summary.AverageCharacterCount.Should().Be(95.2);
        summary.AverageSentimentScore.Should().Be(0.3);
        summary.CategoryDistribution.Should().HaveCount(2);
        summary.MostCommonCategory.Should().Be("Platform");
    }
}
