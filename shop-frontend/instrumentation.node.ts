import { NodeSDK } from '@opentelemetry/sdk-node'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-grpc'
import { OTLPExporterNodeConfigBase } from '@opentelemetry/otlp-exporter-base';
import { Resource } from '@opentelemetry/resources'
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions'
import { SimpleSpanProcessor } from '@opentelemetry/sdk-trace-node'

const otelConfig = {
  url: process.env.OTEL_EXPORTER_OTLP_ENDPOINT,
  headers: {
    'x-sf-service-name': 'shop-frontend',
  },
} as OTLPExporterNodeConfigBase

const sdk = new NodeSDK({
  resource: new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'shop-frontend',
  }),
  spanProcessor: new SimpleSpanProcessor(new OTLPTraceExporter(otelConfig)),
})
sdk.start()