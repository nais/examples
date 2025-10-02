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
    public async Task<ActionResult<List<QuoteAnalytics>>> GetAllAnalytics()
    {
        try
        {
            var analytics = await _analyticsService.GetAllAnalyticsAsync();
            return Ok(analytics);
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
    public async Task<ActionResult<QuoteAnalytics>> GetAnalyticsForQuote(string id)
    {
        try
        {
            var analytics = await _analyticsService.GetAnalyticsForQuoteAsync(id);
            return Ok(analytics);
        }
        catch (InvalidOperationException ex)
        {
            _logger.LogWarning(ex, "Quote {QuoteId} not found", id);
            return NotFound(new { error = "Quote not found", message = ex.Message });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get analytics for quote {QuoteId}", id);
            return StatusCode(500, new { error = "Failed to fetch analytics", message = ex.Message });
        }
    }

    [HttpGet("summary")]
    [ProducesResponseType(typeof(AnalyticsSummary), StatusCodes.Status200OK)]
    public async Task<ActionResult<AnalyticsSummary>> GetSummary()
    {
        try
        {
            var summary = await _analyticsService.GetAnalyticsSummaryAsync();
            return Ok(summary);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get analytics summary");
            return StatusCode(500, new { error = "Failed to fetch summary", message = ex.Message });
        }
    }
}
