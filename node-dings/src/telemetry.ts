'use strict'

import { type Sampler, SamplingDecision, SpanKind } from '@opentelemetry/api'

import { AlwaysOnSampler } from '@opentelemetry/core'
import { registerInstrumentations } from '@opentelemetry/instrumentation'
import { NodeTracerProvider } from '@opentelemetry/sdk-trace-node'
import {
  ConsoleSpanExporter,
  SimpleSpanProcessor
} from '@opentelemetry/sdk-trace-base'
import { Resource } from '@opentelemetry/resources'
import { MeterProvider } from '@opentelemetry/sdk-metrics'
import {
  SemanticAttributes,
  SemanticResourceAttributes
} from '@opentelemetry/semantic-conventions'
import { type SpanAttributes } from '@opentelemetry/api/build/src/trace/attributes'
import { ExpressInstrumentation } from '@opentelemetry/instrumentation-express'
import { HttpInstrumentation } from '@opentelemetry/instrumentation-http'
import { PrometheusExporter } from '@opentelemetry/exporter-prometheus'

// Not functionally required but gives some insight what happens behind the scenes
import opentelemetry, {
  diag,
  DiagConsoleLogger,
  DiagLogLevel
} from '@opentelemetry/api'
diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.INFO)

// getResource returns a Resource with the service name and version
const getResource = (serviceName: string): Resource => {
  const serviceVersion = process.env.SERVICE_VERSION ?? '0.0.0'

  return new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: serviceName,
    [SemanticResourceAttributes.SERVICE_VERSION]: serviceVersion
  })
}

// setupMetrics returns an instance of MeterProvider configured with a
// PrometheusExporter and a ConsoleMetricExporter
export const setupMetrics = (serviceName: string): MeterProvider => {
  // Create a provider for activating and tracking metrics
  const metricProvider = new MeterProvider({
    resource: getResource(serviceName)
  })

  // Export metrics as Prometheus format
  const { endpoint, port } = PrometheusExporter.DEFAULT_OPTIONS
  const prometheusExporter = new PrometheusExporter({ endpoint, port }, () => {
    console.log(`Prometheus listening on http://localhost:${port}${endpoint}`)
  })

  // Periodically export metrics to the Prometheus exporter
  metricProvider.addMetricReader(prometheusExporter)

  // return opentelemetry.metrics.getMeter(serviceName);
  return metricProvider
}

// setupTracing returns an instance of NodeTracerProvider configured with a
// SimpleSpanProcessor and Jaeger Exporter
export const setupTracing = (serviceName: string): NodeTracerProvider => {
  // Create a provider for activating and tracking spans
  const tracerProvider = new NodeTracerProvider({
    resource: getResource(serviceName),
    sampler: filterSampler(ignoreHealthCheck, new AlwaysOnSampler())
  })

  // const options = {
  //   serviceName: process.env.OTEL_SERVICE_NAME,
  //   tags: [], // optional
  //   // You can use the default UDPSender
  //   // host: 'localhost', // optional
  //   // port: 6832, // optional
  //   // OR you can use the HTTPSender as follows
  //   // 14250 : model.proto not working
  //   endpoint: process.env.OTEL_EXPORTER_JAEGER_ENDPOINT,
  //   maxPacketSize: 65000 // optional
  // }

  // tracerProvider.addSpanProcessor(new BatchSpanProcessor(new JaegerExporter(options)));
  tracerProvider.addSpanProcessor(
    new SimpleSpanProcessor(new ConsoleSpanExporter())
  )

  // Initialize the OpenTelemetry APIs to use the NodeTracerProvider bindings
  tracerProvider.register()

  // return opentelemetry.trace.getTracer(serviceName);
  return tracerProvider
}

export const setup = (
  serviceName: string
): { metricsProvider: MeterProvider, tracerProvider: NodeTracerProvider } => {
  const metricsProvider = setupMetrics(serviceName)
  const tracerProvider = setupTracing(serviceName)

  // opentelemetry.trace.setGlobalTracerProvider(tracerProvider);
  opentelemetry.metrics.setGlobalMeterProvider(metricsProvider)

  registerInstrumentations({
    tracerProvider,
    meterProvider: metricsProvider,
    instrumentations: [
      // Express instrumentation expects HTTP layer to be instrumented
      new HttpInstrumentation(),
      new ExpressInstrumentation()
    ]
  })

  return { metricsProvider, tracerProvider }
}

type FilterFunction = (
  spanName: string,
  spanKind: SpanKind,
  attributes: SpanAttributes
) => boolean

function filterSampler (filterFn: FilterFunction, parent: Sampler): Sampler {
  return {
    shouldSample (ctx, tid, spanName, spanKind, attr, links) {
      if (!filterFn(spanName, spanKind, attr)) {
        return { decision: SamplingDecision.NOT_RECORD }
      }
      return parent.shouldSample(ctx, tid, spanName, spanKind, attr, links)
    },
    toString () {
      return `FilterSampler(${parent.toString()})`
    }
  }
}

function ignoreHealthCheck (
  spanName: string,
  spanKind: SpanKind,
  attributes: SpanAttributes
): boolean {
  return (
    spanKind !== SpanKind.SERVER ||
    attributes[SemanticAttributes.HTTP_ROUTE] !== '/health'
  )
}
