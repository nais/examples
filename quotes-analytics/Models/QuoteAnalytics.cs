namespace Nais.QuotesAnalytics.Models;

public record QuoteAnalytics(
    string QuoteId,
    string Text,
    string Author,
    int WordCount,
    int CharacterCount,
    double SentimentScore,
    string Category,
    DateTime AnalyzedAt
);

public record AnalyticsSummary(
    int TotalQuotes,
    double AverageWordCount,
    double AverageCharacterCount,
    double AverageSentimentScore,
    Dictionary<string, int> CategoryDistribution,
    string MostCommonCategory
);
