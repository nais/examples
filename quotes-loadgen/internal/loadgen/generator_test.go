package loadgen

import (
	"context"
	"errors"
	"io"
	"net/http"
	"strings"
	"sync"
	"testing"
	"time"

	"go.opentelemetry.io/otel/trace"
)

type mockHTTPClient struct {
	mu        sync.Mutex
	requests  []string
	responses map[string]*http.Response
	errors    map[string]error
	delay     time.Duration
}

func (m *mockHTTPClient) Do(req *http.Request) (*http.Response, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	if m.delay > 0 {
		time.Sleep(m.delay)
	}

	url := req.URL.String()
	m.requests = append(m.requests, url)

	if err, ok := m.errors[url]; ok {
		return nil, err
	}

	if resp, ok := m.responses[url]; ok {
		return resp, nil
	}

	return &http.Response{
		StatusCode: 200,
		Body:       io.NopCloser(strings.NewReader("OK")),
	}, nil
}

func (m *mockHTTPClient) getRequestCount(url string) int {
	m.mu.Lock()
	defer m.mu.Unlock()

	count := 0
	for _, r := range m.requests {
		if r == url {
			count++
		}
	}
	return count
}

func (m *mockHTTPClient) getAllRequests() []string {
	m.mu.Lock()
	defer m.mu.Unlock()
	return append([]string(nil), m.requests...)
}

type mockLogger struct {
	mu   sync.Mutex
	logs []logEntry
}

type logEntry struct {
	level string
	msg   string
	args  []any
}

func (m *mockLogger) Info(msg string, args ...any) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.logs = append(m.logs, logEntry{level: "info", msg: msg, args: args})
}

func (m *mockLogger) Error(msg string, args ...any) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.logs = append(m.logs, logEntry{level: "error", msg: msg, args: args})
}

func (m *mockLogger) WithGroup(name string) Logger {
	return m
}

func (m *mockLogger) hasErrorLog() bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	for _, log := range m.logs {
		if log.level == "error" {
			return true
		}
	}
	return false
}

func (m *mockLogger) containsMessage(msg string) bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	for _, log := range m.logs {
		if strings.Contains(log.msg, msg) {
			return true
		}
	}
	return false
}

type mockTracer struct{}

func (m *mockTracer) Start(ctx context.Context, spanName string, opts ...trace.SpanStartOption) (context.Context, trace.Span) {
	return ctx, trace.SpanFromContext(ctx)
}

func TestNewLoadGenerator(t *testing.T) {
	client := &mockHTTPClient{}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/test"},
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)

	if lg == nil {
		t.Fatal("NewLoadGenerator returned nil")
	}
	if lg.client != client {
		t.Error("client not set correctly")
	}
	if lg.logger != logger {
		t.Error("logger not set correctly")
	}
	if lg.tracer != tracer {
		t.Error("tracer not set correctly")
	}
}

func TestLoadGenerator_Run_ValidationError(t *testing.T) {
	client := &mockHTTPClient{}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{},
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)
	err := lg.Run(context.Background())

	if err != ErrNoURLsProvided {
		t.Errorf("Run() error = %v, want %v", err, ErrNoURLsProvided)
	}
}

func TestLoadGenerator_Run_WithDuration(t *testing.T) {
	client := &mockHTTPClient{
		responses: make(map[string]*http.Response),
	}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/test"},
		Hostname:          "localhost:8080",
		Protocol:          "http",
		RequestsPerSecond: 5,
		Duration:          200 * time.Millisecond,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)
	ctx := context.Background()

	err := lg.Run(ctx)
	if err != nil {
		t.Errorf("Run() unexpected error = %v", err)
	}

	if !logger.containsMessage("Starting load test") {
		t.Error("Expected starting log message")
	}
	if !logger.containsMessage("Load test completed") {
		t.Error("Expected completion log message")
	}

	time.Sleep(100 * time.Millisecond)
	requests := client.getAllRequests()
	if len(requests) < 1 {
		t.Errorf("Expected at least 1 request to be made, got %d", len(requests))
	}
}

func TestLoadGenerator_Run_Indefinitely(t *testing.T) {
	client := &mockHTTPClient{
		responses: make(map[string]*http.Response),
	}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/test"},
		Hostname:          "localhost:8080",
		Protocol:          "http",
		RequestsPerSecond: 10,
		Duration:          0,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)
	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()

	err := lg.Run(ctx)
	if err != context.DeadlineExceeded {
		t.Errorf("Run() error = %v, want %v", err, context.DeadlineExceeded)
	}

	if !logger.containsMessage("Running load test indefinitely") {
		t.Error("Expected indefinite run log message")
	}
}

func TestLoadGenerator_MakeRequest_Success(t *testing.T) {
	url := "http://localhost:8080/test"
	client := &mockHTTPClient{
		responses: map[string]*http.Response{
			url: {
				StatusCode: 200,
				Body:       io.NopCloser(strings.NewReader("OK")),
			},
		},
	}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/test"},
		Hostname:          "localhost:8080",
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)
	lg.makeRequest(context.Background(), url)

	if client.getRequestCount(url) != 1 {
		t.Errorf("Expected 1 request, got %d", client.getRequestCount(url))
	}

	if !logger.containsMessage("Loaded URL") {
		t.Error("Expected success log message")
	}

	if logger.hasErrorLog() {
		t.Error("Unexpected error log")
	}
}

func TestLoadGenerator_MakeRequest_HTTPError(t *testing.T) {
	url := "http://localhost:8080/test"
	client := &mockHTTPClient{
		errors: map[string]error{
			url: errors.New("connection refused"),
		},
	}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/test"},
		Hostname:          "localhost:8080",
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)
	lg.makeRequest(context.Background(), url)

	if !logger.hasErrorLog() {
		t.Error("Expected error log")
	}

	if !logger.containsMessage("Error loading URL") {
		t.Error("Expected error loading URL message")
	}
}

func TestLoadGenerator_MakeRequest_MultipleURLs(t *testing.T) {
	urls := []string{
		"http://localhost:8080/api/v1",
		"http://localhost:8080/api/v2",
		"http://localhost:8080/health",
	}

	client := &mockHTTPClient{
		responses: make(map[string]*http.Response),
	}
	logger := &mockLogger{}
	tracer := &mockTracer{}
	config := Config{
		URLs:              []string{"/api/v1", "/api/v2", "/health"},
		Hostname:          "localhost:8080",
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	lg := NewLoadGenerator(client, logger, tracer, config)

	for _, url := range urls {
		lg.makeRequest(context.Background(), url)
	}

	allRequests := client.getAllRequests()
	if len(allRequests) != 3 {
		t.Errorf("Expected 3 requests, got %d", len(allRequests))
	}

	for _, url := range urls {
		if client.getRequestCount(url) != 1 {
			t.Errorf("Expected 1 request for %s, got %d", url, client.getRequestCount(url))
		}
	}
}

func TestLoadGenerator_LogStartMessage(t *testing.T) {
	tests := []struct {
		name            string
		config          Config
		expectedMessage string
	}{
		{
			name: "with duration",
			config: Config{
				URLs:              []string{"/test"},
				Hostname:          "localhost:8080",
				Protocol:          "http",
				RequestsPerSecond: 10,
				Duration:          10 * time.Second,
			},
			expectedMessage: "Starting load test",
		},
		{
			name: "indefinite run",
			config: Config{
				URLs:              []string{"/test"},
				Hostname:          "localhost:8080",
				Protocol:          "http",
				RequestsPerSecond: 10,
				Duration:          0,
			},
			expectedMessage: "Running load test indefinitely",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			logger := &mockLogger{}
			lg := NewLoadGenerator(&mockHTTPClient{}, logger, &mockTracer{}, tt.config)

			urls := tt.config.GetFullURLs()
			lg.logStartMessage(urls)

			if !logger.containsMessage(tt.expectedMessage) {
				t.Errorf("Expected log message containing %q", tt.expectedMessage)
			}
		})
	}
}
