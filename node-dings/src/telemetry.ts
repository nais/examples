"use strict";

import { Sampler, SpanKind } from "@opentelemetry/api";

const opentelemetry = require("@opentelemetry/api");

// Not functionally required but gives some insight what happens behind the scenes
const { diag, DiagConsoleLogger, DiagLogLevel } = opentelemetry;
diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.INFO);

import { AlwaysOnSampler } from "@opentelemetry/core";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { NodeTracerProvider } from "@opentelemetry/sdk-trace-node";
import { SimpleSpanProcessor } from "@opentelemetry/sdk-trace-base";
import { JaegerExporter } from "@opentelemetry/exporter-jaeger";
import { ZipkinExporter } from "@opentelemetry/exporter-zipkin";
import { Resource } from "@opentelemetry/resources";
import {
  ConsoleMetricExporter,
  MeterProvider,
  PeriodicExportingMetricReader,
} from "@opentelemetry/sdk-metrics";
import {
  SemanticAttributes,
  SemanticResourceAttributes,
} from "@opentelemetry/semantic-conventions";
import { SpanAttributes } from "@opentelemetry/api/build/src/trace/attributes";

const Exporter = (process.env.EXPORTER || "").toLowerCase().startsWith("z")
  ? ZipkinExporter
  : JaegerExporter;
import { ExpressInstrumentation } from "@opentelemetry/instrumentation-express";
import { PrometheusExporter } from "@opentelemetry/exporter-prometheus";
const { HttpInstrumentation } = require("@opentelemetry/instrumentation-http");

const getResource = (serviceName: string) => {
  const serviceVersion = process.env.SERVICE_VERSION || "0.0.0";

  return new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: serviceName,
    [SemanticResourceAttributes.SERVICE_VERSION]: serviceVersion,
  });
};

// setupMetrics returns an instance of MeterProvider configured with a
// PrometheusExporter and a ConsoleMetricExporter
export const setupMetrics = (serviceName: string) => {
  // Create a provider for activating and tracking metrics
  const metricProvider = new MeterProvider({
    resource: getResource(serviceName),
  });

  // Export metrics as Prometheus format
  const { endpoint, port } = PrometheusExporter.DEFAULT_OPTIONS;
  const prometheusExporter = new PrometheusExporter({ endpoint, port }, () => {
    console.log(`Prometheus listening on http://localhost:${port}${endpoint}`);
  });

  // Periodically export metrics to the Prometheus exporter
  metricProvider.addMetricReader(prometheusExporter);

  // return opentelemetry.metrics.getMeter(serviceName);
  return metricProvider;
};

// setupTracing returns an instance of NodeTracerProvider configured with a
// SimpleSpanProcessor and Jaeger Exporter
export const setupTracing = (serviceName: string) => {
  // Create a provider for activating and tracking spans
  const tracerProvider = new NodeTracerProvider({
    resource: getResource(serviceName),
    sampler: filterSampler(ignoreHealthCheck, new AlwaysOnSampler()),
  });

  const exporter = new Exporter({
    serviceName,
  });

  tracerProvider.addSpanProcessor(new SimpleSpanProcessor(exporter));

  // Initialize the OpenTelemetry APIs to use the NodeTracerProvider bindings
  tracerProvider.register();

  // return opentelemetry.trace.getTracer(serviceName);
  return tracerProvider;
};

export const setup = (serviceName: string) => {
  const metricsProvider = setupMetrics(serviceName);
  const tracerProvider = setupTracing(serviceName);

  // opentelemetry.trace.setGlobalTracerProvider(tracerProvider);
  opentelemetry.metrics.setGlobalMeterProvider(metricsProvider);

  registerInstrumentations({
    tracerProvider: tracerProvider,
    meterProvider: metricsProvider,
    instrumentations: [
      // Express instrumentation expects HTTP layer to be instrumented
      HttpInstrumentation,
      ExpressInstrumentation,
    ],
  });

  return { metricsProvider, tracerProvider };
};

type FilterFunction = (
  spanName: string,
  spanKind: SpanKind,
  attributes: SpanAttributes
) => boolean;

function filterSampler(filterFn: FilterFunction, parent: Sampler): Sampler {
  return {
    shouldSample(ctx, tid, spanName, spanKind, attr, links) {
      if (!filterFn(spanName, spanKind, attr)) {
        return { decision: opentelemetry.SamplingDecision.NOT_RECORD };
      }
      return parent.shouldSample(ctx, tid, spanName, spanKind, attr, links);
    },
    toString() {
      return `FilterSampler(${parent.toString()})`;
    },
  };
}

function ignoreHealthCheck(
  spanName: string,
  spanKind: SpanKind,
  attributes: SpanAttributes
) {
  return (
    spanKind !== opentelemetry.SpanKind.SERVER ||
    attributes[SemanticAttributes.HTTP_ROUTE] !== "/health"
  );
}
