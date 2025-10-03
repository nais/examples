package loadgen

import (
	"errors"
	"testing"
)

func TestErrors(t *testing.T) {
	tests := []struct {
		name     string
		err      error
		expected string
	}{
		{
			name:     "ErrNoURLsProvided",
			err:      ErrNoURLsProvided,
			expected: "no URLs provided",
		},
		{
			name:     "ErrInvalidProtocol",
			err:      ErrInvalidProtocol,
			expected: "invalid protocol, must be http or https",
		},
		{
			name:     "ErrInvalidRPS",
			err:      ErrInvalidRPS,
			expected: "requests per second must be greater than 0",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if tt.err.Error() != tt.expected {
				t.Errorf("Error message = %v, expected %v", tt.err.Error(), tt.expected)
			}
		})
	}
}

func TestErrors_AreDistinct(t *testing.T) {
	if errors.Is(ErrNoURLsProvided, ErrInvalidProtocol) {
		t.Error("ErrNoURLsProvided and ErrInvalidProtocol should be distinct")
	}
	if errors.Is(ErrNoURLsProvided, ErrInvalidRPS) {
		t.Error("ErrNoURLsProvided and ErrInvalidRPS should be distinct")
	}
	if errors.Is(ErrInvalidProtocol, ErrInvalidRPS) {
		t.Error("ErrInvalidProtocol and ErrInvalidRPS should be distinct")
	}
}

func TestErrors_CanBeCompared(t *testing.T) {
	err := ErrNoURLsProvided

	if !errors.Is(err, ErrNoURLsProvided) {
		t.Error("errors.Is should work with ErrNoURLsProvided")
	}

	config := Config{
		URLs:              []string{},
		Protocol:          "http",
		RequestsPerSecond: 10,
	}

	validationErr := config.Validate()
	if !errors.Is(validationErr, ErrNoURLsProvided) {
		t.Error("Validation error should be ErrNoURLsProvided")
	}
}
