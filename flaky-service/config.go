package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"strconv"
	"strings"

	"github.com/Unleash/unleash-client-go/v3"
	"github.com/Unleash/unleash-client-go/v3/api"
)

const (
	AppName               = "flaky-service"
	unleashDefaultProject = "default"
	unleashDefaultEnv     = "development"
	unleashDefaultType    = "client"

	toggleFlakinessLevelName    = "flaky-service.flakiness-level"
	toggleFlakinessLevelDefault = 50
)

type Config struct {
	Server  *ServerConfig
	Unleash *UnleashConfig
}

type ServerConfig struct {
	Port string `env:"PORT, default=8080"`
	Host string `env:"HOST, default=0.0.0.0"`
}

type UnleashConfig struct {
	ClientType string `env:"UNLEASH_SERVER_API_TYPE, default=client"`
	Projects   string `env:"UNLEASH_SERVER_API_PROJECTS, default=default"`
	Env        string `env:"UNLEASH_SERVER_API_ENV, default=development"`
	Url        string `env:"UNLEASH_SERVER_API_URL"`
	Token      string `env:"UNLEASH_SERVER_API_TOKEN"`
}

func (c *Config) UnleashInit() {
	projects := strings.Split(c.Unleash.Projects, ",")
	project := unleashDefaultProject
	if len(projects) > 0 {
		project = projects[0]
	}

	unleash.Initialize(
		// unleash.WithListener(&unleash.DebugListener{}),
		unleash.WithAppName(AppName),
		unleash.WithEnvironment(c.Unleash.Env),
		unleash.WithUrl(fmt.Sprintf("%s/api", c.Unleash.Url)),
		unleash.WithProjectName(project),
		unleash.WithCustomHeaders(http.Header{"Authorization": {fmt.Sprintf("Bearer %s", c.Unleash.Token)}}),
	)
}

func (c *Config) FlakinessLevel() int {
	variant := unleash.GetVariant(toggleFlakinessLevelName, unleash.WithVariantFallback(api.GetDefaultVariant()))

	if variant == nil {
		slog.Info("Failed to get variant", "featureToggleName", toggleFlakinessLevelName)
		return toggleFlakinessLevelDefault
	}

	if variant.Payload.Type != "number" {
		slog.Info("Invalid variant payload type", "featureToggleName", toggleFlakinessLevelName, "type", variant.Payload.Type, "value", variant.Payload.Value)
		return toggleFlakinessLevelDefault
	}

	value, err := strconv.Atoi(variant.Payload.Value)
	if err != nil {
		slog.Info("Failed to parse variant value", "featureToggleName", toggleFlakinessLevelName, "type", variant.Payload.Type, "value", variant.Payload.Value, "error", err)
		return toggleFlakinessLevelDefault
	}
	return value
}
