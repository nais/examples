using System.Diagnostics;
using System.Diagnostics.Metrics;
using System.Text.Json;
using Nais.QuotesAnalytics.Models;

namespace Nais.QuotesAnalytics.Services;

public class QuotesAnalyticsService
{
    public const string ActivitySourceName = "Nais.QuotesAnalytics";
    public const string MeterName = "Nais.QuotesAnalytics";

    private static readonly ActivitySource ActivitySource = new(ActivitySourceName);
    private static readonly Meter Meter = new(MeterName);

    private readonly HttpClient _httpClient;
    private readonly ILogger<QuotesAnalyticsService> _logger;
    private readonly Counter<long> _quotesAnalyzedCounter;
    private readonly Histogram<double> _wordCountHistogram;
    private readonly Histogram<double> _sentimentScoreHistogram;
    private readonly Counter<long> _categoryCounter;
    private readonly Histogram<long> _analysisTimeHistogram;
    private readonly Dictionary<string, QuoteAnalytics> _analyticsCache = new();

    public QuotesAnalyticsService(HttpClient httpClient, ILogger<QuotesAnalyticsService> logger)
    {
        _httpClient = httpClient;
        _logger = logger;

        _quotesAnalyzedCounter = Meter.CreateCounter<long>(
            "quotes.analyzed.total",
            description: "Total number of quotes analyzed");

        _wordCountHistogram = Meter.CreateHistogram<double>(
            "quotes.word.count",
            description: "Distribution of word counts in quotes");

        _sentimentScoreHistogram = Meter.CreateHistogram<double>(
            "quotes.sentiment.score",
            description: "Distribution of sentiment scores");

        _categoryCounter = Meter.CreateCounter<long>(
            "quotes.category.total",
            description: "Total number of quotes by category");

        _analysisTimeHistogram = Meter.CreateHistogram<long>(
            "quotes.analysis.duration.ms",
            description: "Time taken to analyze a quote in milliseconds");
    }

    public async Task<List<QuoteAnalytics>> GetAllAnalyticsAsync()
    {
        using var activity = ActivitySource.StartActivity("GetAllAnalytics", ActivityKind.Internal);

        try
        {
            _logger.LogInformation("Fetching all quotes for analysis");

            var response = await _httpClient.GetAsync("/api/quotes");
            response.EnsureSuccessStatusCode();

            var quotes = await response.Content.ReadFromJsonAsync<List<Quote>>();

            if (quotes == null || quotes.Count == 0)
            {
                _logger.LogWarning("No quotes found in backend");
                activity?.SetTag("quotes.count", 0);
                return new List<QuoteAnalytics>();
            }

            activity?.SetTag("quotes.count", quotes.Count);
            _logger.LogInformation("Found {Count} quotes to analyze", quotes.Count);

            var analytics = new List<QuoteAnalytics>();
            foreach (var quote in quotes)
            {
                if (quote.Id != null)
                {
                    var analysis = await AnalyzeQuoteAsync(quote);
                    analytics.Add(analysis);
                }
            }

            return analytics;
        }
        catch (HttpRequestException ex)
        {
            _logger.LogError(ex, "Failed to fetch quotes from backend");
            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            activity?.AddEvent(new ActivityEvent("exception",
                tags: new ActivityTagsCollection
                {
                    { "exception.type", ex.GetType().FullName },
                    { "exception.message", ex.Message }
                }));
            throw;
        }
    }

    public async Task<QuoteAnalytics> GetAnalyticsForQuoteAsync(string quoteId)
    {
        using var activity = ActivitySource.StartActivity("GetAnalyticsForQuote", ActivityKind.Internal);
        activity?.SetTag("quote.id", quoteId);

        try
        {
            // Check cache first
            if (_analyticsCache.TryGetValue(quoteId, out var cachedAnalytics))
            {
                _logger.LogInformation("Returning cached analytics for quote {QuoteId}", quoteId);
                activity?.SetTag("cache.hit", true);
                return cachedAnalytics;
            }

            activity?.SetTag("cache.hit", false);
            _logger.LogInformation("Fetching quote {QuoteId} from backend", quoteId);

            var response = await _httpClient.GetAsync($"/api/quotes/{quoteId}");
            response.EnsureSuccessStatusCode();

            var quote = await response.Content.ReadFromJsonAsync<Quote>();

            if (quote == null)
            {
                throw new InvalidOperationException($"Quote {quoteId} not found");
            }

            return await AnalyzeQuoteAsync(quote);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogError(ex, "Failed to fetch quote {QuoteId} from backend", quoteId);
            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            activity?.AddEvent(new ActivityEvent("exception",
                tags: new ActivityTagsCollection
                {
                    { "exception.type", ex.GetType().FullName },
                    { "exception.message", ex.Message }
                }));
            throw;
        }
    }

    public async Task<AnalyticsSummary> GetAnalyticsSummaryAsync()
    {
        using var activity = ActivitySource.StartActivity("GetAnalyticsSummary", ActivityKind.Internal);

        try
        {
            var allAnalytics = await GetAllAnalyticsAsync();

            if (allAnalytics.Count == 0)
            {
                return new AnalyticsSummary(
                    TotalQuotes: 0,
                    AverageWordCount: 0,
                    AverageCharacterCount: 0,
                    AverageSentimentScore: 0,
                    CategoryDistribution: new Dictionary<string, int>(),
                    MostCommonCategory: "N/A"
                );
            }

            var categoryDistribution = allAnalytics
                .GroupBy(a => a.Category)
                .ToDictionary(g => g.Key, g => g.Count());

            var mostCommonCategory = categoryDistribution
                .OrderByDescending(kvp => kvp.Value)
                .First()
                .Key;

            var summary = new AnalyticsSummary(
                TotalQuotes: allAnalytics.Count,
                AverageWordCount: allAnalytics.Average(a => a.WordCount),
                AverageCharacterCount: allAnalytics.Average(a => a.CharacterCount),
                AverageSentimentScore: allAnalytics.Average(a => a.SentimentScore),
                CategoryDistribution: categoryDistribution,
                MostCommonCategory: mostCommonCategory
            );

            activity?.SetTag("summary.total_quotes", summary.TotalQuotes);
            activity?.SetTag("summary.most_common_category", summary.MostCommonCategory);

            _logger.LogInformation(
                "Generated analytics summary: {TotalQuotes} quotes, most common category: {Category}",
                summary.TotalQuotes,
                summary.MostCommonCategory);

            return summary;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to generate analytics summary");
            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            activity?.AddEvent(new ActivityEvent("exception",
                tags: new ActivityTagsCollection
                {
                    { "exception.type", ex.GetType().FullName },
                    { "exception.message", ex.Message }
                }));
            throw;
        }
    }

    private async Task<QuoteAnalytics> AnalyzeQuoteAsync(Quote quote)
    {
        var stopwatch = Stopwatch.StartNew();

        using var activity = ActivitySource.StartActivity("AnalyzeQuote", ActivityKind.Internal);
        activity?.SetTag("quote.id", quote.Id);
        activity?.SetTag("quote.author", quote.Author);

        try
        {
            _logger.LogDebug("Analyzing quote {QuoteId}", quote.Id);

            // Simulate some processing with custom spans
            using (var span = ActivitySource.StartActivity("CalculateWordCount", ActivityKind.Internal))
            {
                await Task.Delay(Random.Shared.Next(5, 15)); // Simulate work
            }

            var wordCount = CountWords(quote.Text);
            var characterCount = quote.Text.Length;

            using (var span = ActivitySource.StartActivity("CalculateSentiment", ActivityKind.Internal))
            {
                await Task.Delay(Random.Shared.Next(10, 30)); // Simulate work
            }

            var sentimentScore = CalculateSentimentScore(quote.Text);

            using (var span = ActivitySource.StartActivity("CategorizeQuote", ActivityKind.Internal))
            {
                await Task.Delay(Random.Shared.Next(5, 10)); // Simulate work
            }

            var category = CategorizeQuote(quote.Text, quote.Author);

            var analytics = new QuoteAnalytics(
                QuoteId: quote.Id ?? "unknown",
                Text: quote.Text,
                Author: quote.Author,
                WordCount: wordCount,
                CharacterCount: characterCount,
                SentimentScore: sentimentScore,
                Category: category,
                AnalyzedAt: DateTime.UtcNow
            );

            // Cache the result
            if (quote.Id != null)
            {
                _analyticsCache[quote.Id] = analytics;
            }

            stopwatch.Stop();

            // Record custom metrics
            _quotesAnalyzedCounter.Add(1,
                new KeyValuePair<string, object?>("category", category),
                new KeyValuePair<string, object?>("author", quote.Author));

            _wordCountHistogram.Record(wordCount,
                new KeyValuePair<string, object?>("category", category));

            _sentimentScoreHistogram.Record(sentimentScore,
                new KeyValuePair<string, object?>("category", category));

            _categoryCounter.Add(1,
                new KeyValuePair<string, object?>("category", category));

            _analysisTimeHistogram.Record(stopwatch.ElapsedMilliseconds,
                new KeyValuePair<string, object?>("category", category));

            activity?.SetTag("analytics.word_count", wordCount);
            activity?.SetTag("analytics.sentiment_score", sentimentScore);
            activity?.SetTag("analytics.category", category);
            activity?.SetTag("analytics.processing_time_ms", stopwatch.ElapsedMilliseconds);

            _logger.LogInformation(
                "Analyzed quote {QuoteId}: {WordCount} words, sentiment: {Sentiment:F2}, category: {Category}, took {Duration}ms",
                quote.Id,
                wordCount,
                sentimentScore,
                category,
                stopwatch.ElapsedMilliseconds);

            return analytics;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to analyze quote {QuoteId}", quote.Id);
            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            activity?.AddEvent(new ActivityEvent("exception",
                tags: new ActivityTagsCollection
                {
                    { "exception.type", ex.GetType().FullName },
                    { "exception.message", ex.Message }
                }));
            throw;
        }
    }

    private static int CountWords(string text)
    {
        return text.Split(new[] { ' ', '\t', '\n', '\r' }, StringSplitOptions.RemoveEmptyEntries).Length;
    }

    private static double CalculateSentimentScore(string text)
    {
        // Simple sentiment analysis based on positive/negative keywords
        var positiveWords = new[] { "confidence", "simple", "best", "secure", "great", "love", "excellent", "amazing" };
        var negativeWords = new[] { "complex", "difficult", "bad", "hate", "terrible", "awful", "failure" };

        var lowerText = text.ToLowerInvariant();
        var positiveCount = positiveWords.Count(word => lowerText.Contains(word));
        var negativeCount = negativeWords.Count(word => lowerText.Contains(word));

        // Score between -1 (negative) and 1 (positive)
        var totalWords = CountWords(text);
        var score = (positiveCount - negativeCount) / (double)Math.Max(totalWords, 1);

        // Normalize to -1 to 1 range with some randomness
        return Math.Clamp(score + (Random.Shared.NextDouble() - 0.5) * 0.3, -1.0, 1.0);
    }

    private static string CategorizeQuote(string text, string author)
    {
        var lowerText = text.ToLowerInvariant();

        if (lowerText.Contains("deploy") || lowerText.Contains("kubernetes") || lowerText.Contains("platform"))
            return "Platform";

        if (lowerText.Contains("secure") || lowerText.Contains("security"))
            return "Security";

        if (lowerText.Contains("devops") || lowerText.Contains("continuous"))
            return "DevOps";

        if (lowerText.Contains("cloud") || lowerText.Contains("helm"))
            return "Cloud Native";

        if (author.Contains("Nais") || author.Contains("Platform"))
            return "Platform";

        return "General";
    }
}
