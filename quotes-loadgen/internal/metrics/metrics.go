package metrics

import (
	"fmt"
	"net/http"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

var (
	RequestsTotal = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "loadgen_requests_total",
			Help: "Total number of requests made by the load generator",
		},
		[]string{"url", "status"},
	)
	RequestDuration = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "loadgen_request_duration_seconds",
			Help:    "Histogram of request durations",
			Buckets: prometheus.DefBuckets,
		},
		[]string{"url"},
	)
	registered = false
)

func Register() {
	if registered {
		return
	}
	prometheus.MustRegister(RequestsTotal)
	prometheus.MustRegister(RequestDuration)
	registered = true
}

func StartMetricsServer(port int) {
	http.Handle("/metrics", promhttp.Handler())
	go func() {
		if err := http.ListenAndServe(fmt.Sprintf(":%d", port), nil); err != nil {
			// Server stopped, this is expected when shutting down
			fmt.Printf("Metrics server stopped: %v\n", err)
		}
	}()
}
