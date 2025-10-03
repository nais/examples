package loadgen

import (
	"os"
	"testing"
	"time"

	"github.com/spf13/viper"
)

func TestBuildConfigFromViper(t *testing.T) {
	tests := []struct {
		name       string
		setupViper func()
		wantErr    bool
		validate   func(*testing.T, *Config)
	}{
		{
			name: "valid config from environment variables",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/api/v1,/api/v2,/health")
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "http")
				viper.Set("RPS", 10)
				viper.Set("DURATION", 30)
				viper.Set("METRICS", true)
				viper.Set("METRICS_PORT", 9090)
			},
			wantErr: false,
			validate: func(t *testing.T, c *Config) {
				if len(c.URLs) != 3 {
					t.Errorf("Expected 3 URLs, got %d", len(c.URLs))
				}
				if c.URLs[0] != "/api/v1" {
					t.Errorf("Expected first URL to be /api/v1, got %s", c.URLs[0])
				}
				if c.Hostname != "localhost:8080" {
					t.Errorf("Expected hostname localhost:8080, got %s", c.Hostname)
				}
				if c.Protocol != "http" {
					t.Errorf("Expected protocol http, got %s", c.Protocol)
				}
				if c.RequestsPerSecond != 10 {
					t.Errorf("Expected RPS 10, got %d", c.RequestsPerSecond)
				}
				if c.Duration != 30*time.Second {
					t.Errorf("Expected duration 30s, got %v", c.Duration)
				}
				if !c.MetricsEnabled {
					t.Error("Expected metrics to be enabled")
				}
				if c.MetricsPort != 9090 {
					t.Errorf("Expected metrics port 9090, got %d", c.MetricsPort)
				}
			},
		},
		{
			name: "URLs with whitespace should be trimmed",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/api/v1 , /api/v2 , /health ")
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "http")
				viper.Set("RPS", 10)
				viper.Set("DURATION", 10)
			},
			wantErr: false,
			validate: func(t *testing.T, c *Config) {
				if len(c.URLs) != 3 {
					t.Errorf("Expected 3 URLs, got %d", len(c.URLs))
				}
				if c.URLs[0] != "/api/v1" {
					t.Errorf("Expected first URL to be /api/v1, got '%s'", c.URLs[0])
				}
				if c.URLs[1] != "/api/v2" {
					t.Errorf("Expected second URL to be /api/v2, got '%s'", c.URLs[1])
				}
				if c.URLs[2] != "/health" {
					t.Errorf("Expected third URL to be /health, got '%s'", c.URLs[2])
				}
			},
		},
		{
			name: "infinite duration with zero DURATION",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/test")
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "http")
				viper.Set("RPS", 5)
				viper.Set("DURATION", 0)
			},
			wantErr: false,
			validate: func(t *testing.T, c *Config) {
				if c.Duration != 0 {
					t.Errorf("Expected infinite duration (0), got %v", c.Duration)
				}
			},
		},
		{
			name: "https protocol",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/secure")
				viper.Set("HOSTNAME", "example.com")
				viper.Set("PROTOCOL", "https")
				viper.Set("RPS", 5)
				viper.Set("DURATION", 10)
			},
			wantErr: false,
			validate: func(t *testing.T, c *Config) {
				if c.Protocol != "https" {
					t.Errorf("Expected protocol https, got %s", c.Protocol)
				}
			},
		},
		{
			name: "no URLs should fail validation",
			setupViper: func() {
				viper.Reset()
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "http")
				viper.Set("RPS", 10)
				viper.Set("DURATION", 10)
			},
			wantErr: true,
		},
		{
			name: "invalid protocol should fail validation",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/test")
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "ftp")
				viper.Set("RPS", 10)
				viper.Set("DURATION", 10)
			},
			wantErr: true,
		},
		{
			name: "zero RPS should fail validation",
			setupViper: func() {
				viper.Reset()
				viper.Set("URLS", "/test")
				viper.Set("HOSTNAME", "localhost:8080")
				viper.Set("PROTOCOL", "http")
				viper.Set("RPS", 0)
				viper.Set("DURATION", 10)
			},
			wantErr: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tt.setupViper()
			config, err := buildConfigFromViper()

			if tt.wantErr {
				if err == nil {
					t.Error("Expected error but got none")
				}
				return
			}

			if err != nil {
				t.Errorf("Unexpected error: %v", err)
				return
			}

			if tt.validate != nil {
				tt.validate(t, config)
			}
		})
	}
}

func TestBuildConfigFromViper_URLsFromStringSlice(t *testing.T) {
	viper.Reset()
	viper.Set("URLS", []string{"/api/v1", "/api/v2"})
	viper.Set("HOSTNAME", "localhost:8080")
	viper.Set("PROTOCOL", "http")
	viper.Set("RPS", 10)
	viper.Set("DURATION", 10)

	config, err := buildConfigFromViper()
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if len(config.URLs) != 2 {
		t.Errorf("Expected 2 URLs, got %d", len(config.URLs))
	}
}

func TestNewLoadCommand(t *testing.T) {
	cmd := NewLoadCommand()

	if cmd == nil {
		t.Fatal("NewLoadCommand returned nil")
	}

	if cmd.Use != "load" {
		t.Errorf("Expected Use to be 'load', got '%s'", cmd.Use)
	}

	if cmd.Short == "" {
		t.Error("Expected Short description to be set")
	}

	if cmd.RunE == nil {
		t.Error("Expected RunE to be set")
	}

	expectedFlags := []string{"url", "hostname", "protocol", "rps", "duration", "metrics", "metrics-port"}
	for _, flag := range expectedFlags {
		if cmd.Flags().Lookup(flag) == nil {
			t.Errorf("Expected flag '%s' to be defined", flag)
		}
	}
}

func TestSetupFlags(t *testing.T) {
	cmd := NewLoadCommand()

	tests := []struct {
		flagName     string
		flagType     string
		defaultValue interface{}
	}{
		{"url", "stringSlice", nil},
		{"hostname", "string", "localhost:3000"},
		{"protocol", "string", "http"},
		{"rps", "int", 10},
		{"duration", "duration", 10 * time.Second},
		{"metrics", "bool", false},
		{"metrics-port", "int", 8080},
	}

	for _, tt := range tests {
		t.Run(tt.flagName, func(t *testing.T) {
			flag := cmd.Flags().Lookup(tt.flagName)
			if flag == nil {
				t.Fatalf("Flag '%s' not found", tt.flagName)
			}

			if tt.defaultValue != nil && flag.DefValue == "" {
				t.Errorf("Flag '%s' has no default value", tt.flagName)
			}
		})
	}
}

func TestBuildConfigFromViper_WithEnvironmentVariables(t *testing.T) {
	os.Setenv("LOADGEN_URLS", "/env/test1,/env/test2")
	os.Setenv("LOADGEN_HOSTNAME", "env-host:9000")
	os.Setenv("LOADGEN_PROTOCOL", "https")
	os.Setenv("LOADGEN_RPS", "20")
	os.Setenv("LOADGEN_DURATION", "60")
	os.Setenv("LOADGEN_METRICS", "true")
	os.Setenv("LOADGEN_METRICS_PORT", "8888")

	defer func() {
		os.Unsetenv("LOADGEN_URLS")
		os.Unsetenv("LOADGEN_HOSTNAME")
		os.Unsetenv("LOADGEN_PROTOCOL")
		os.Unsetenv("LOADGEN_RPS")
		os.Unsetenv("LOADGEN_DURATION")
		os.Unsetenv("LOADGEN_METRICS")
		os.Unsetenv("LOADGEN_METRICS_PORT")
	}()

	viper.Reset()
	viper.SetEnvPrefix("LOADGEN")
	viper.AutomaticEnv()
	_ = viper.BindEnv("URLS")
	_ = viper.BindEnv("HOSTNAME")
	_ = viper.BindEnv("PROTOCOL")
	_ = viper.BindEnv("RPS")
	_ = viper.BindEnv("DURATION")
	_ = viper.BindEnv("METRICS")
	_ = viper.BindEnv("METRICS_PORT")

	config, err := buildConfigFromViper()
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if len(config.URLs) != 2 {
		t.Errorf("Expected 2 URLs from env, got %d", len(config.URLs))
	}
	if config.Hostname != "env-host:9000" {
		t.Errorf("Expected hostname from env, got %s", config.Hostname)
	}
	if config.Protocol != "https" {
		t.Errorf("Expected https protocol from env, got %s", config.Protocol)
	}
	if config.RequestsPerSecond != 20 {
		t.Errorf("Expected RPS 20 from env, got %d", config.RequestsPerSecond)
	}
	if config.Duration != 60*time.Second {
		t.Errorf("Expected duration 60s from env, got %v", config.Duration)
	}
	if !config.MetricsEnabled {
		t.Error("Expected metrics enabled from env")
	}
	if config.MetricsPort != 8888 {
		t.Errorf("Expected metrics port 8888 from env, got %d", config.MetricsPort)
	}
}
