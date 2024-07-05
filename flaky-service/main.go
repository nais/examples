package main

import (
	"context"
	"fmt"
	"log"
	"log/slog"
	"net/http"

	"github.com/joho/godotenv"
	"github.com/sethvargo/go-envconfig"
	"golang.org/x/exp/rand"
)

var c Config

func init() {
	ctx := context.Background()

	err := godotenv.Load()
	if err != nil {
		slog.Info("Error loading .env file", "error", err)
	}

	if err := envconfig.Process(ctx, &c); err != nil {
		log.Fatal("Error processing config:", err)
	}

	c.UnleashInit()
}

func main() {
	slog.Info("Starting server", "flakinessLevel", c.Server.Host, "port", c.Server.Port)

	// Start a simple HTTP server
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		level := c.FlakinessLevel()
		slog.Info("Flakiness level", "level", level)

		w.Header().Set("X-Flakiness-Level", fmt.Sprintf("%d", level))

		if rand.Intn(100) < level {
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte(`{"error": "Internal Server Error"}`))
			return
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"message": "Hello, World!"}`))
	})

	http.HandlerFunc("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	log.Fatal(http.ListenAndServe(fmt.Sprintf("%s:%s", c.Server.Host, c.Server.Port), nil))
}
