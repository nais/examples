package main

import (
	"github.com/nais/examples/quotes-loadgen/internal/loadgen"
	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "quotes-loadgen",
	Short: "A load generator for testing URLs",
}

func main() {
	rootCmd.AddCommand(loadgen.NewLoadCommand())
	if err := rootCmd.Execute(); err != nil {
		panic(err)
	}
}
