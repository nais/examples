package loadgen

import (
	"fmt"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	"log/slog"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	urls              []string
	duration          int
	requestsPerSecond int
	hostname          string
	protocol          string
	logger            *slog.Logger

	metricsEnabled bool
	metricsPort    int
	requestsTotal  = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "loadgen_requests_total",
			Help: "Total number of requests made by the load generator",
		},
		[]string{"url", "status"},
	)
	requestDuration = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "loadgen_request_duration_seconds",
			Help:    "Histogram of request durations",
			Buckets: prometheus.DefBuckets,
		},
		[]string{"url"},
	)
)

func init() {
	logger = slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{}))

	// Initialize viper with environment variable prefix
	viper.SetEnvPrefix("LOADGEN")
	viper.AutomaticEnv()

	prometheus.MustRegister(requestsTotal)
	prometheus.MustRegister(requestDuration)
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
							start := time.Now()
							resp, err := http.Get(url)
							if err != nil {
								logger.Error("Error loading URL", slog.String("url", url), slog.Any("error", err))
								if metricsEnabled {
									requestsTotal.WithLabelValues(url, "error").Inc()
								}
							} else {
								logger.Info("Loaded URL", "url", url, "status", resp.StatusCode)
								if metricsEnabled {
									requestsTotal.WithLabelValues(url, http.StatusText(resp.StatusCode)).Inc()
									requestDuration.WithLabelValues(url).Observe(time.Since(start).Seconds())
								}
								resp.Body.Close()
							}
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
			startMetricsServer()
		}
	}

	return cmd
}

func startMetricsServer() {
	http.Handle("/metrics", promhttp.Handler())
	go http.ListenAndServe(fmt.Sprintf(":%d", metricsPort), nil)
}
