package main

import (
	"context"
	"log/slog"

	"github.com/go-logr/logr"
	"go.opentelemetry.io/contrib/exporters/autoexport"
	"go.opentelemetry.io/contrib/propagators/b3"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/metric"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.24.0"
)

type Telemetry struct {
	TelemetryConfig *TelemetryConfig
}

type TelemetryConfig struct {
	Resource *resource.Resource `yaml:"resource"`
}

func AutoTelemetryConfig() *TelemetryConfig {
	return &TelemetryConfig{
		Resource: resource.Environment(),
	}
}

func NewTelemetry(cfg *TelemetryConfig, logger *slog.Logger) (*Telemetry, error) {
	otel.SetLogger(logr.FromSlogHandler(logger.Handler()))

	resourceAttr := cfg.Resource.Attributes()

	resource := resource.NewWithAttributes(
		semconv.SchemaURL,
		resourceAttr...,
	)

	spanExporter, err := newSpanExporter()
	if err != nil {
		return nil, err
	}

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithResource(resource),
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithBatcher(spanExporter),
	)
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(propagation.TraceContext{},
		propagation.Baggage{}, b3.New()))

	metricsReader, err := newMetricReader()
	if err != nil {
		return nil, err
	}

	provider := sdkmetric.NewMeterProvider(
		sdkmetric.WithReader(
			metricsReader,
		),
		sdkmetric.WithResource(resource),
	)

	otel.SetMeterProvider(provider)

	return &Telemetry{
		TelemetryConfig: cfg,
	}, nil
}

// NewTelemetryMock returns Telemetry object with NoOp loggers, meters, tracers
func NewTelemetryMock() *Telemetry {
	return &Telemetry{
		TelemetryConfig: AutoTelemetryConfig(),
	}
}

func newMetricReader() (sdkmetric.Reader, error) {
	return autoexport.NewMetricReader(context.Background(),
		autoexport.WithFallbackMetricReader(func(_ context.Context) (sdkmetric.Reader, error) {
			return metric.NewManualReader(), nil
		}),
	)
}

func newSpanExporter() (sdktrace.SpanExporter, error) {
	return autoexport.NewSpanExporter(context.Background(), autoexport.WithFallbackSpanExporter(
		func(_ context.Context) (sdktrace.SpanExporter, error) {
			return noopSpanExporter{}, nil
		},
	))
}

type noopSpanExporter struct{}

var _ trace.SpanExporter = noopSpanExporter{}

func (e noopSpanExporter) ExportSpans(_ context.Context, _ []trace.ReadOnlySpan) error {
	return nil
}

func (e noopSpanExporter) Shutdown(_ context.Context) error {
	return nil
}
