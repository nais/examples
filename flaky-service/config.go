package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"strconv"
	"strings"

	"github.com/Unleash/unleash-client-go/v4"
	"github.com/Unleash/unleash-client-go/v4/api"
)

const (
	AppName = "flaky-service"
)

type Config struct {
	Server  *ServerConfig
	Unleash *UnleashConfig
	Feature *FeatureConfig
}

type FeatureConfig struct {
	// Flakiness level feature toggle default value
	FlakinessLevelDefaultValue int `env:"FEATURE_FLAKINESS_LEVEL_DEFAULT_VALUE, default=50"`

	// Flakiness level feature toggle name in Unleash
	FlakinessLevelToggleName string `env:"FEATURE_FLAKINESS_LEVEL_TOGGLE_NAME, default=flaky-service.flakiness-level"`
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
	project := "default"
	projects := strings.Split(c.Unleash.Projects, ",")
	if len(projects) > 0 {
		project = projects[0]
	}

	unleash.Initialize(
		//unleash.WithListener(&unleash.DebugListener{}),
		unleash.WithAppName(AppName),
		unleash.WithEnvironment(c.Unleash.Env),
		unleash.WithUrl(fmt.Sprintf("%s/api", c.Unleash.Url)),
		unleash.WithProjectName(project),
		unleash.WithCustomHeaders(http.Header{"Authorization": {fmt.Sprintf("Bearer %s", c.Unleash.Token)}}),
	)
}

func (c *Config) FlakinessLevel(logger *slog.Logger) int {
	variant := unleash.GetVariant(c.Feature.FlakinessLevelToggleName, unleash.WithVariantFallback(api.GetDefaultVariant()))
	if variant == nil {
		logger.Info("Failed to get variant", "featureToggleName", c.Feature.FlakinessLevelToggleName)
		return c.Feature.FlakinessLevelDefaultValue
	}

	if !variant.FeatureEnabled {
		logger.Info("Flakiness level feature toggle not enabled, reverting to default value", "featureToggleName", c.Feature.FlakinessLevelToggleName, "defaultValue", c.Feature.FlakinessLevelDefaultValue)
		return c.Feature.FlakinessLevelDefaultValue
	}

	if variant.Payload.Type != "number" {
		logger.Info("Invalid variant payload type", "featureToggleName", c.Feature.FlakinessLevelToggleName, "type", variant.Payload.Type, "value", variant.Payload.Value)
		return c.Feature.FlakinessLevelDefaultValue
	}

	value, err := strconv.Atoi(variant.Payload.Value)
	if err != nil {
		logger.Info("Failed to parse variant value", "featureToggleName", c.Feature.FlakinessLevelToggleName, "type", variant.Payload.Type, "value", variant.Payload.Value, "error", err)
		return c.Feature.FlakinessLevelDefaultValue
	}
	return value
}
