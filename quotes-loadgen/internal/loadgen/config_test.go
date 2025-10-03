package loadgen

import (
	"testing"
	"time"
)

func TestConfig_Validate(t *testing.T) {
	tests := []struct {
		name    string
		config  Config
		wantErr error
	}{
		{
			name: "valid config with all fields",
			config: Config{
				URLs:              []string{"/api/v1", "/api/v2"},
				Hostname:          "localhost:8080",
				Protocol:          "http",
				RequestsPerSecond: 10,
				Duration:          10 * time.Second,
				MetricsEnabled:    true,
				MetricsPort:       8080,
			},
			wantErr: nil,
		},
		{
			name: "valid config with https",
			config: Config{
				URLs:              []string{"/api/v1"},
				Hostname:          "example.com",
				Protocol:          "https",
				RequestsPerSecond: 5,
				Duration:          0,
			},
			wantErr: nil,
		},
		{
			name: "no URLs provided",
			config: Config{
				URLs:              []string{},
				Protocol:          "http",
				RequestsPerSecond: 10,
			},
			wantErr: ErrNoURLsProvided,
		},
		{
			name: "nil URLs",
			config: Config{
				URLs:              nil,
				Protocol:          "http",
				RequestsPerSecond: 10,
			},
			wantErr: ErrNoURLsProvided,
		},
		{
			name: "invalid protocol",
			config: Config{
				URLs:              []string{"/api/v1"},
				Protocol:          "ftp",
				RequestsPerSecond: 10,
			},
			wantErr: ErrInvalidProtocol,
		},
		{
			name: "empty protocol",
			config: Config{
				URLs:              []string{"/api/v1"},
				Protocol:          "",
				RequestsPerSecond: 10,
			},
			wantErr: ErrInvalidProtocol,
		},
		{
			name: "zero requests per second",
			config: Config{
				URLs:              []string{"/api/v1"},
				Protocol:          "http",
				RequestsPerSecond: 0,
			},
			wantErr: ErrInvalidRPS,
		},
		{
			name: "negative requests per second",
			config: Config{
				URLs:              []string{"/api/v1"},
				Protocol:          "http",
				RequestsPerSecond: -5,
			},
			wantErr: ErrInvalidRPS,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := tt.config.Validate()
			if err != tt.wantErr {
				t.Errorf("Config.Validate() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestConfig_GetFullURLs(t *testing.T) {
	tests := []struct {
		name     string
		config   Config
		expected []string
	}{
		{
			name: "with hostname and http protocol",
			config: Config{
				URLs:     []string{"/api/v1", "/api/v2", "/health"},
				Hostname: "localhost:8080",
				Protocol: "http",
			},
			expected: []string{
				"http://localhost:8080/api/v1",
				"http://localhost:8080/api/v2",
				"http://localhost:8080/health",
			},
		},
		{
			name: "with hostname and https protocol",
			config: Config{
				URLs:     []string{"/api/v1", "/api/v2"},
				Hostname: "example.com",
				Protocol: "https",
			},
			expected: []string{
				"https://example.com/api/v1",
				"https://example.com/api/v2",
			},
		},
		{
			name: "without hostname returns original URLs",
			config: Config{
				URLs:     []string{"http://example.com/api/v1", "https://other.com/api/v2"},
				Hostname: "",
				Protocol: "http",
			},
			expected: []string{
				"http://example.com/api/v1",
				"https://other.com/api/v2",
			},
		},
		{
			name: "with hostname containing port",
			config: Config{
				URLs:     []string{"/test"},
				Hostname: "localhost:3000",
				Protocol: "http",
			},
			expected: []string{
				"http://localhost:3000/test",
			},
		},
		{
			name: "empty URLs slice",
			config: Config{
				URLs:     []string{},
				Hostname: "localhost:8080",
				Protocol: "http",
			},
			expected: []string{},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := tt.config.GetFullURLs()
			if len(result) != len(tt.expected) {
				t.Fatalf("GetFullURLs() returned %d URLs, expected %d", len(result), len(tt.expected))
			}
			for i, url := range result {
				if url != tt.expected[i] {
					t.Errorf("GetFullURLs()[%d] = %v, expected %v", i, url, tt.expected[i])
				}
			}
		})
	}
}
