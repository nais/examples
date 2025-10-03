package loadgen

import (
	"time"
)

type Config struct {
	URLs              []string
	Hostname          string
	Protocol          string
	RequestsPerSecond int
	Duration          time.Duration
	MetricsEnabled    bool
	MetricsPort       int
}

func (c *Config) GetFullURLs() []string {
	if c.Hostname == "" {
		return c.URLs
	}

	fullURLs := make([]string, len(c.URLs))
	for i, url := range c.URLs {
		fullURLs[i] = c.Protocol + "://" + c.Hostname + url
	}
	return fullURLs
}

func (c *Config) Validate() error {
	if len(c.URLs) == 0 {
		return ErrNoURLsProvided
	}
	if c.Protocol != "http" && c.Protocol != "https" {
		return ErrInvalidProtocol
	}
	if c.RequestsPerSecond <= 0 {
		return ErrInvalidRPS
	}
	return nil
}
