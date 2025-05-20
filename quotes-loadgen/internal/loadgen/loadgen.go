package loadgen

import (
	"context"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	"log/slog"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"

	"github.com/nais/examples/quotes-loadgen/internal/metrics"
	"github.com/nais/examples/quotes-loadgen/internal/otel"
	t "go.opentelemetry.io/otel/trace"
)

var (
	tracer            t.Tracer
	urls              []string
	duration          int
	requestsPerSecond int
	hostname          string
	protocol          string
	logger            *slog.Logger

	metricsEnabled bool
	metricsPort    int
)

func init() {
	// Initialize OpenTelemetry Tracing
	ctx := context.Background()
	tracer, _ = otel.InitTracer(ctx)
	logger = slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{}))

	// Initialize viper with environment variable prefix
	viper.SetEnvPrefix("LOADGEN")
	viper.AutomaticEnv()

	metrics.Register()
}

func NewLoadCommand() *cobra.Command {
	logger = logger.WithGroup("loadgen")

	cmd := &cobra.Command{
		Use:   "load",
		Short: "Generate load on a set of URLs",
		Run: func(cmd *cobra.Command, args []string) {
			if len(urls) == 0 {
				logger.Info("Please provide a set of URLs using the --url flag or the URLS environment variable.")
				return
			}

			urlList := make([]string, len(urls))
			copy(urlList, urls)
			if hostname != "" {
				for i, url := range urlList {
					urlList[i] = protocol + "://" + hostname + url
				}
			}
			if duration == 0 {
				logger.Info("Running load test indefinitely", slog.Int("rps", requestsPerSecond), slog.String("urls", strings.Join(urlList, ",")), slog.String("hostname", hostname), slog.String("protocol", protocol))
			} else {
				logger.Info("Starting load test", slog.Int("duration", duration), slog.Int("rps", requestsPerSecond), slog.String("urls", strings.Join(urlList, ",")), slog.String("hostname", hostname), slog.String("protocol", protocol))
			}

			var wg sync.WaitGroup
			stop := make(chan struct{})

			// Start load generation
			client := http.Client{
				Transport: otelhttp.NewTransport(http.DefaultTransport),
			}
			for _, url := range urlList {
				for i := 0; i < requestsPerSecond; i++ {
					wg.Add(1)
					go func(url string) {
						defer wg.Done()
						for {
							if duration > 0 {
								select {
								case <-stop:
									return
								default:
								}
							}
							// Parent span for the request
							ctx, parentSpan := tracer.Start(context.Background(), "loadgen.request")
							start := time.Now()
							// Child span for the HTTP GET
							ctx, httpSpan := tracer.Start(ctx, "http.get")
							req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
							if err != nil {
								logger.Error("Error creating request", slog.String("url", url), slog.Any("error", err))
								if metricsEnabled {
									metrics.RequestsTotal.WithLabelValues(url, "error").Inc()
								}
								httpSpan.End()
								parentSpan.End()
								continue
							}
							resp, err := client.Do(req)
							if err != nil {
								logger.Error("Error loading URL", slog.String("url", url), slog.Any("error", err))
								if metricsEnabled {
									metrics.RequestsTotal.WithLabelValues(url, "error").Inc()
								}
							} else {
								logger.Info("Loaded URL", "url", url, "status", resp.StatusCode)
								if metricsEnabled {
									metrics.RequestsTotal.WithLabelValues(url, http.StatusText(resp.StatusCode)).Inc()
									metrics.RequestDuration.WithLabelValues(url).Observe(time.Since(start).Seconds())
								}
								resp.Body.Close()
							}
							httpSpan.End()
							parentSpan.End()
							time.Sleep(time.Second / time.Duration(requestsPerSecond))
						}
					}(url)
				}
			}
			// Stop load generation after the specified duration
			if duration != 0 {
				time.Sleep(time.Duration(duration) * time.Second)
				close(stop)
				wg.Wait()
				logger.Info("Load test completed.")
			} else {
				// Wait indefinitely if duration is 0
				wg.Wait()
			}
		},
	}

	// Bind flags to viper keys
	cmd.Flags().StringSliceVar(&urls, "url", nil, "List of URLs to load (can be specified multiple times or set via URLS environment variable)")
	viper.BindPFlag("URLS", cmd.Flags().Lookup("url"))

	cmd.Flags().StringVar(&hostname, "hostname", "localhost:3000", "Hostname to prefix to all URLs (can be set via HOSTNAME environment variable)")
	viper.BindPFlag("HOSTNAME", cmd.Flags().Lookup("hostname"))

	cmd.Flags().StringVar(&protocol, "protocol", "http", "Protocol to use for URLs (http or https, can be set via PROTOCOL environment variable)")
	viper.BindPFlag("PROTOCOL", cmd.Flags().Lookup("protocol"))

	cmd.Flags().IntVar(&requestsPerSecond, "rps", 10, "Number of requests per second (can be set via RPS environment variable)")
	viper.BindPFlag("RPS", cmd.Flags().Lookup("rps"))

	cmd.Flags().IntVar(&duration, "duration", 10, "Duration of the load test in seconds (can be set via DURATION environment variable)")
	viper.BindPFlag("DURATION", cmd.Flags().Lookup("duration"))

	cmd.Flags().BoolVar(&metricsEnabled, "metrics", false, "Enable Prometheus metrics endpoint")
	cmd.Flags().IntVar(&metricsPort, "metrics-port", 8080, "Port for the Prometheus metrics endpoint")

	viper.BindPFlag("METRICS", cmd.Flags().Lookup("metrics"))
	viper.BindPFlag("METRICS_PORT", cmd.Flags().Lookup("metrics-port"))

	cmd.PreRun = func(cmd *cobra.Command, args []string) {
		// Load values from viper
		urls = viper.GetStringSlice("URLS")
		hostname = viper.GetString("HOSTNAME")
		protocol = viper.GetString("PROTOCOL")
		requestsPerSecond = viper.GetInt("RPS")
		duration = viper.GetInt("DURATION")

		metricsEnabled = viper.GetBool("METRICS")
		metricsPort = viper.GetInt("METRICS_PORT")

		if protocol != "http" && protocol != "https" {
			logger.Error("Invalid protocol specified", slog.String("protocol", protocol))
			return
		}

		if hostname != "" {
			if strings.Contains(hostname, "http://") || strings.Contains(hostname, "https://") {
				logger.Error("Invalid hostname specified. Hostname should not include protocol.", slog.String("hostname", hostname))
				return
			}
		}

		if metricsEnabled {
			logger.Info("Starting Prometheus metrics server", slog.Int("port", metricsPort))
			metrics.StartMetricsServer(metricsPort)
		}
	}

	return cmd
}
