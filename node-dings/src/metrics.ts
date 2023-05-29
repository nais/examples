import { MeterProvider } from "@opentelemetry/sdk-metrics";
import { PrometheusExporter } from "@opentelemetry/exporter-prometheus";
import { MetricOptions } from "@opentelemetry/api";

const { endpoint, port } = PrometheusExporter.DEFAULT_OPTIONS;

const exporter = new PrometheusExporter({}, () => {
  console.log(
    `prometheus scrape endpoint: http://localhost:${port}${endpoint}`
  );
});

// Creates MeterProvider and installs the exporter as a MetricReader
const meterProvider = new MeterProvider();
meterProvider.addMetricReader(exporter);
const meter = meterProvider.getMeter("example-prometheus");

const counter = meter.createCounter("http_response_status_count", {
  description: "Example of a counter",
});

const histogram = meter.createHistogram("http_response", {
  description: "Example of a histogram",
});

export { exporter, counter, histogram };
