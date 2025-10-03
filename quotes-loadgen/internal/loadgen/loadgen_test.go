package loadgen

import (
	"log/slog"
	"os"
	"strings"
	"testing"
)

func TestNewSlogLogger(t *testing.T) {
	handler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{})
	slogger := slog.New(handler)

	logger := NewSlogLogger(slogger)

	if logger == nil {
		t.Fatal("NewSlogLogger returned nil")
	}

	_, ok := logger.(*slogLogger)
	if !ok {
		t.Error("NewSlogLogger did not return *slogLogger")
	}
}

func TestSlogLogger_WithGroup(t *testing.T) {
	handler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{})
	slogger := slog.New(handler)
	logger := NewSlogLogger(slogger)

	groupLogger := logger.WithGroup("test-group")

	if groupLogger == nil {
		t.Fatal("WithGroup returned nil")
	}

	_, ok := groupLogger.(*slogLogger)
	if !ok {
		t.Error("WithGroup did not return *slogLogger")
	}
}

func TestSlogLogger_Info(t *testing.T) {
	var buf strings.Builder
	handler := slog.NewJSONHandler(&buf, &slog.HandlerOptions{})
	slogger := slog.New(handler)
	logger := NewSlogLogger(slogger)

	logger.Info("test message", slog.String("key", "value"))

	output := buf.String()
	if !strings.Contains(output, "test message") {
		t.Error("Info log did not contain expected message")
	}
}

func TestSlogLogger_Error(t *testing.T) {
	var buf strings.Builder
	handler := slog.NewJSONHandler(&buf, &slog.HandlerOptions{})
	slogger := slog.New(handler)
	logger := NewSlogLogger(slogger)

	logger.Error("error message", slog.String("error", "test error"))

	output := buf.String()
	if !strings.Contains(output, "error message") {
		t.Error("Error log did not contain expected message")
	}
}
