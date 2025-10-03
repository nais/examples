package loadgen

import (
	"context"
	"net/http"
	"strconv"
	"time"

	"log/slog"

	"github.com/nais/examples/quotes-loadgen/internal/metrics"
	"go.opentelemetry.io/otel/trace"
)

type HTTPClient interface {
	Do(req *http.Request) (*http.Response, error)
}

type Logger interface {
	Info(msg string, args ...any)
	Error(msg string, args ...any)
	WithGroup(name string) Logger
}

type Tracer interface {
	Start(ctx context.Context, spanName string, opts ...trace.SpanStartOption) (context.Context, trace.Span)
}

type LoadGenerator struct {
	client HTTPClient
	logger Logger
	tracer Tracer
	config Config
}

func NewLoadGenerator(client HTTPClient, logger Logger, tracer Tracer, config Config) *LoadGenerator {
	return &LoadGenerator{
		client: client,
		logger: logger,
		tracer: tracer,
		config: config,
	}
}

func (lg *LoadGenerator) Run(ctx context.Context) error {
	if err := lg.config.Validate(); err != nil {
		return err
	}

	urls := lg.config.GetFullURLs()
	lg.logStartMessage(urls)

	if lg.config.Duration == 0 {
		return lg.runIndefinitely(ctx, urls)
	}
	return lg.runForDuration(ctx, urls)
}

func (lg *LoadGenerator) logStartMessage(urls []string) {
	if lg.config.Duration == 0 {
		lg.logger.Info("Running load test indefinitely",
			slog.Int("rps", lg.config.RequestsPerSecond),
			slog.Any("urls", urls),
			slog.String("hostname", lg.config.Hostname),
			slog.String("protocol", lg.config.Protocol))
	} else {
		lg.logger.Info("Starting load test",
			slog.Int("duration_seconds", int(lg.config.Duration.Seconds())),
			slog.Int("rps", lg.config.RequestsPerSecond),
			slog.Any("urls", urls),
			slog.String("hostname", lg.config.Hostname),
			slog.String("protocol", lg.config.Protocol))
	}
}

func (lg *LoadGenerator) runIndefinitely(ctx context.Context, urls []string) error {
	for _, url := range urls {
		for i := 0; i < lg.config.RequestsPerSecond; i++ {
			go lg.makeRequests(ctx, url)
		}
	}
	<-ctx.Done()
	return ctx.Err()
}

func (lg *LoadGenerator) runForDuration(ctx context.Context, urls []string) error {
	timeoutCtx, cancel := context.WithTimeout(ctx, lg.config.Duration)
	defer cancel()

	for _, url := range urls {
		for i := 0; i < lg.config.RequestsPerSecond; i++ {
			go lg.makeRequests(timeoutCtx, url)
		}
	}

	<-timeoutCtx.Done()
	if timeoutCtx.Err() == context.DeadlineExceeded {
		lg.logger.Info("Load test completed")
		return nil
	}
	return timeoutCtx.Err()
}

func (lg *LoadGenerator) makeRequests(ctx context.Context, url string) {
	interval := time.Second / time.Duration(lg.config.RequestsPerSecond)
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			lg.makeRequest(ctx, url)
		}
	}
}

func (lg *LoadGenerator) makeRequest(ctx context.Context, url string) {
	parentCtx, parentSpan := lg.tracer.Start(ctx, "loadgen.request")
	defer parentSpan.End()

	httpCtx, httpSpan := lg.tracer.Start(parentCtx, "http.get")
	defer httpSpan.End()

	start := time.Now()

	req, err := http.NewRequestWithContext(httpCtx, http.MethodGet, url, nil)
	if err != nil {
		lg.logger.Error("Error creating request", slog.String("url", url), slog.Any("error", err))
		return
	}

	resp, err := lg.client.Do(req)
	duration := time.Since(start)

	if err != nil {
		metrics.RequestsTotal.WithLabelValues(url, "error").Inc()
		lg.logger.Error("Error loading URL", slog.String("url", url), slog.Any("error", err))
		return
	}
	defer resp.Body.Close()

	statusCode := strconv.Itoa(resp.StatusCode)
	metrics.RequestsTotal.WithLabelValues(url, statusCode).Inc()
	metrics.RequestDuration.WithLabelValues(url).Observe(duration.Seconds())

	lg.logger.Info("Loaded URL",
		slog.String("url", url),
		slog.Int("status", resp.StatusCode),
		slog.Duration("duration", duration),
		slog.String("trace_id", trace.SpanFromContext(httpCtx).SpanContext().TraceID().String()))
}
