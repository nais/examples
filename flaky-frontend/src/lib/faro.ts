import { Faro, getWebInstrumentations, initializeFaro, LogLevel } from '@grafana/faro-web-sdk'
import { TracingInstrumentation } from '@grafana/faro-web-tracing'

import getConfig from 'next/config'

const { publicRuntimeConfig } = getConfig()

let faro: Faro | null = null
export function initInstrumentation(): void {
  if (typeof window === 'undefined' || faro !== null || publicRuntimeConfig.env !== "production") return

  getFaro()
}

export function getFaro(): Faro {
  if (faro != null) return faro
  faro = initializeFaro({
    url: publicRuntimeConfig.faroUrl,
    app: {
      name: publicRuntimeConfig.faroAppName,
      namespace: publicRuntimeConfig.faroNamespace,
    },
    instrumentations: [
      ...getWebInstrumentations({
        captureConsole: true,
      }),
      new TracingInstrumentation(),
    ],
  })
  return faro
}