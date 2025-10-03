// Package loadgen provides load testing functionality for HTTP endpoints.
// This package offers a testable load generator that can be configured
// and used independently of command-line interfaces.
package loadgen

import (
	"log/slog"

	"github.com/nais/examples/quotes-loadgen/internal/metrics"
)

// slogLogger wraps slog.Logger to implement the Logger interface
type slogLogger struct {
	*slog.Logger
}

func (s *slogLogger) WithGroup(name string) Logger {
	return &slogLogger{s.Logger.WithGroup(name)}
}

// NewSlogLogger creates a new slog-based logger that implements the Logger interface
func NewSlogLogger(logger *slog.Logger) Logger {
	return &slogLogger{logger}
}

func init() {
	metrics.Register()
}
