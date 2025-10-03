package loadgen

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"

	"github.com/nais/examples/quotes-loadgen/internal/metrics"
	"github.com/nais/examples/quotes-loadgen/internal/otel"
)

func NewLoadCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "load",
		Short: "Generate load on a set of URLs",
		RunE: func(cmd *cobra.Command, args []string) error {
			config, err := buildConfigFromViper()
			if err != nil {
				return fmt.Errorf("invalid configuration: %w", err)
			}

			if config.MetricsEnabled {
				fmt.Printf("Starting Prometheus metrics server on port %d\n", config.MetricsPort)
				metrics.StartMetricsServer(config.MetricsPort)
			}

			ctx := context.Background()
			tracer, cleanup := otel.InitTracer(ctx)
			defer cleanup()

			client := &http.Client{
				Transport: otelhttp.NewTransport(http.DefaultTransport),
			}

			logger := NewSlogLogger(
				slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{})).WithGroup("loadgen"),
			)

			generator := NewLoadGenerator(client, logger, tracer, *config)
			return generator.Run(ctx)
		},
	}

	setupFlags(cmd)
	return cmd
}

func setupFlags(cmd *cobra.Command) {
	cmd.Flags().StringSlice("url", nil, "List of URLs to load")
	cmd.Flags().String("hostname", "localhost:3000", "Hostname to prefix to all URLs")
	cmd.Flags().String("protocol", "http", "Protocol to use for URLs (http or https)")
	cmd.Flags().Int("rps", 10, "Number of requests per second")
	cmd.Flags().Duration("duration", 10*time.Second, "Duration of the load test")
	cmd.Flags().Bool("metrics", false, "Enable Prometheus metrics endpoint")
	cmd.Flags().Int("metrics-port", 8080, "Port for the Prometheus metrics endpoint")

	viper.SetEnvPrefix("LOADGEN")
	viper.AutomaticEnv()

	_ = viper.BindPFlag("URLS", cmd.Flags().Lookup("url"))
	_ = viper.BindPFlag("HOSTNAME", cmd.Flags().Lookup("hostname"))
	_ = viper.BindPFlag("PROTOCOL", cmd.Flags().Lookup("protocol"))
	_ = viper.BindPFlag("RPS", cmd.Flags().Lookup("rps"))
	_ = viper.BindPFlag("DURATION", cmd.Flags().Lookup("duration"))
	_ = viper.BindPFlag("METRICS", cmd.Flags().Lookup("metrics"))
	_ = viper.BindPFlag("METRICS_PORT", cmd.Flags().Lookup("metrics-port"))
}

func buildConfigFromViper() (*Config, error) {
	var urls []string

	urlsFromEnv := viper.GetString("URLS")
	if urlsFromEnv != "" {
		urls = strings.Split(urlsFromEnv, ",")
		// Trim whitespace from each URL
		for i, url := range urls {
			urls[i] = strings.TrimSpace(url)
		}
	} else {
		urls = viper.GetStringSlice("URLS")
	}

	durationSeconds := viper.GetInt("DURATION")
	var duration time.Duration
	if durationSeconds == 0 {
		duration = 0 // infinite
	} else {
		duration = time.Duration(durationSeconds) * time.Second
	}

	config := &Config{
		URLs:              urls,
		Hostname:          viper.GetString("HOSTNAME"),
		Protocol:          viper.GetString("PROTOCOL"),
		RequestsPerSecond: viper.GetInt("RPS"),
		Duration:          duration,
		MetricsEnabled:    viper.GetBool("METRICS"),
		MetricsPort:       viper.GetInt("METRICS_PORT"),
	}

	return config, config.Validate()
}
