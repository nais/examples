using Microsoft.AspNetCore.Mvc;
using Nais.QuotesAnalytics.Models;
using Nais.QuotesAnalytics.Services;

namespace Nais.QuotesAnalytics.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AnalyticsController : ControllerBase
{
    private readonly QuotesAnalyticsService _analyticsService;
    private readonly ILogger<AnalyticsController> _logger;

    public AnalyticsController(
        QuotesAnalyticsService analyticsService,
        ILogger<AnalyticsController> logger)
    {
        _analyticsService = analyticsService;
        _logger = logger;
    }

    [HttpGet]
    [ProducesResponseType(typeof(List<QuoteAnalytics>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status500InternalServerError)]
    public async Task<ActionResult<List<QuoteAnalytics>>> GetAllAnalytics()
    {
        try
        {
            var analytics = await _analyticsService.GetAllAnalyticsAsync();
            _logger.LogInformation("Retrieved {Count} analytics records", analytics.Count);
            return Ok(analytics);
        }
        catch (InvalidOperationException ex) when (ex.Message.Contains("request URI"))
        {
            _logger.LogError(ex, "HttpClient configuration error");
            return StatusCode(503, new { error = "Backend service unavailable", message = "Service configuration error" });
        }
        catch (HttpRequestException ex)
        {
            _logger.LogError(ex, "Failed to connect to quotes backend service");
            return StatusCode(503, new { error = "Backend service unavailable", message = "Unable to connect to quotes service" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get all analytics");
            return StatusCode(500, new { error = "Failed to fetch analytics", message = ex.Message });
        }
    }

    [HttpGet("{id}")]
    [ProducesResponseType(typeof(QuoteAnalytics), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status500InternalServerError)]
    public async Task<ActionResult<QuoteAnalytics>> GetAnalyticsForQuote(string id)
    {
        if (string.IsNullOrWhiteSpace(id))
        {
            return BadRequest(new { error = "Invalid quote ID", message = "Quote ID cannot be empty" });
        }

        try
        {
            var analytics = await _analyticsService.GetAnalyticsForQuoteAsync(id);
            _logger.LogInformation("Retrieved analytics for quote {QuoteId}", id);
            return Ok(analytics);
        }
        catch (InvalidOperationException ex) when (ex.Message.Contains("request URI"))
        {
            _logger.LogError(ex, "HttpClient configuration error for quote {QuoteId}", id);
            return StatusCode(503, new { error = "Backend service unavailable", message = "Service configuration error" });
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning(ex, "Quote {QuoteId} not found", id);
            return NotFound(new { error = "Quote not found", message = ex.Message, quoteId = id });
        }
        catch (HttpRequestException ex)
        {
            _logger.LogError(ex, "Failed to connect to quotes backend service for quote {QuoteId}", id);
            return StatusCode(503, new { error = "Backend service unavailable", message = "Unable to connect to quotes service" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get analytics for quote {QuoteId}", id);
            return StatusCode(500, new { error = "Failed to fetch analytics", message = ex.Message });
        }
    }

    [HttpGet("summary")]
    [ProducesResponseType(typeof(AnalyticsSummary), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status500InternalServerError)]
    public async Task<ActionResult<AnalyticsSummary>> GetSummary()
    {
        try
        {
            var summary = await _analyticsService.GetAnalyticsSummaryAsync();
            _logger.LogInformation("Retrieved analytics summary with {TotalQuotes} quotes", summary.TotalQuotes);
            return Ok(summary);
        }
        catch (InvalidOperationException ex) when (ex.Message.Contains("request URI"))
        {
            _logger.LogError(ex, "HttpClient configuration error");
            return StatusCode(503, new { error = "Backend service unavailable", message = "Service configuration error" });
        }
        catch (HttpRequestException ex)
        {
            _logger.LogError(ex, "Failed to connect to quotes backend service");
            return StatusCode(503, new { error = "Backend service unavailable", message = "Unable to connect to quotes service" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get analytics summary");
            return StatusCode(500, new { error = "Failed to fetch summary", message = ex.Message });
        }
    }
}
