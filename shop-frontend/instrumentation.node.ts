import { NodeSDK } from '@opentelemetry/sdk-node'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { SimpleSpanProcessor } from '@opentelemetry/sdk-trace-node'
import { getNodeAutoInstrumentations, } from '@opentelemetry/auto-instrumentations-node'

const sdk = new NodeSDK({
  spanProcessor: new SimpleSpanProcessor(new OTLPTraceExporter()),
  instrumentations: [getNodeAutoInstrumentations()]
})
sdk.start()