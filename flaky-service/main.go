package main

import (
	"context"
	"fmt"
	"log"
	"log/slog"
	"net/http"

	"github.com/joho/godotenv"
	"github.com/sethvargo/go-envconfig"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/metric"
	"golang.org/x/exp/rand"
)

var c Config

func init() {
	ctx := context.Background()

	err := godotenv.Load()
	if err != nil {
		slog.Info("Error loading .env file", "error", err)
	}

	if err := envconfig.Process(ctx, &c); err != nil {
		log.Fatal("Error processing config:", err)
	}

	c.UnleashInit()
	NewTelemetry(AutoTelemetryConfig())
}

func main() {
	slog.Info("Starting server", "host", c.Server.Host, "port", c.Server.Port)

	tp := otel.GetTracerProvider()
	tracer := tp.Tracer("flaky-service")

	mp := otel.GetMeterProvider()
	meter := mp.Meter("flaky-service")
	requestsCounter, err := meter.Int64Counter("http_requests", metric.WithDescription("Number of HTTP requests."))
	if err != nil {
		panic("Counter setup failed")
	}

	// Start a simple HTTP server
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		ctx, span := tracer.Start(r.Context(), "hello-span")
		defer span.End()

		w.Header().Set("Content-Type", "application/json")

		level := c.FlakinessLevel()
		slog.Info("Flakiness level", "level", level)

		w.Header().Set("X-Flakiness-Level", fmt.Sprintf("%d", level))

		if rand.Intn(100) < level {
			span.RecordError(fmt.Errorf("you found me"))
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte(`{"error": "Internal Server Error"}`))

			requestsCounter.Add(ctx, 1, metric.WithAttributes(
				attribute.Key("path").String("/"),
				attribute.Key("status_code").Int(500),
			))
			return
		}

		requestsCounter.Add(ctx, 1, metric.WithAttributes(
			attribute.Key("path").String("/"),
			attribute.Key("status_code").Int(200),
		))

		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"message": "Hello, World!"}`))
	})

	http.HandleFunc("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status": "ok"}`))
	})

	log.Fatal(http.ListenAndServe(fmt.Sprintf("%s:%s", c.Server.Host, c.Server.Port), nil))
}
