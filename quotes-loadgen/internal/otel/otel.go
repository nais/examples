package otel

import (
	"context"
	"log"
	"net"
	"os"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.17.0"
	oteltrace "go.opentelemetry.io/otel/trace"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
)

// InitTracer sets up OpenTelemetry tracing using environment variables.
func InitTracer(ctx context.Context) (oteltrace.Tracer, func()) {
	endpoint := os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
	insecure := os.Getenv("OTEL_EXPORTER_OTLP_INSECURE") == "true"
	serviceName := os.Getenv("OTEL_SERVICE_NAME")
	if serviceName == "" {
		serviceName = "quotes-loadgen"
	}

	res, err := resource.New(ctx,
		resource.WithFromEnv(),
		resource.WithProcess(),
		resource.WithTelemetrySDK(),
		resource.WithHost(),
		resource.WithAttributes(
			semconv.ServiceName(serviceName),
		),
	)
	if err != nil {
		log.Printf("failed to create resource: %v", err)
	}

	var opts []otlptracegrpc.Option
	if endpoint != "" {
		opts = append(opts, otlptracegrpc.WithEndpoint(endpoint))
	}
	if insecure {
		opts = append(opts, otlptracegrpc.WithDialOption(grpc.WithTransportCredentials(insecureCreds{})))
	}

	exporter, err := otlptracegrpc.New(ctx, opts...)
	if err != nil {
		log.Printf("failed to create OTLP trace exporter: %v", err)
		return nil, func() {}
	}

	bsp := trace.NewBatchSpanProcessor(exporter)
	tp := trace.NewTracerProvider(
		trace.WithResource(res),
		trace.WithSpanProcessor(bsp),
		trace.WithSampler(trace.AlwaysSample()),
	)

	otel.SetTracerProvider(tp)

	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(
		propagation.TraceContext{},
		propagation.Baggage{},
	))

	tracer := tp.Tracer(serviceName)
	return tracer, func() {
		_ = tp.Shutdown(ctx)
	}
}

// insecureCreds implements grpc.TransportCredentials for insecure connection
// (copied from original loadgen.go)
type insecureCreds struct{}

func (insecureCreds) ClientHandshake(ctx context.Context, s string, conn net.Conn) (net.Conn, credentials.AuthInfo, error) {
	return conn, nil, nil
}
func (insecureCreds) ServerHandshake(conn net.Conn) (net.Conn, credentials.AuthInfo, error) {
	return conn, nil, nil
}
func (insecureCreds) Info() credentials.ProtocolInfo          { return credentials.ProtocolInfo{} }
func (insecureCreds) Clone() credentials.TransportCredentials { return insecureCreds{} }
func (insecureCreds) OverrideServerName(string) error         { return nil }
