/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  swcMinify: true,
  serverRuntimeConfig: {
    // Standalone mode resolves all variables at build time
    // https://github.com/vercel/next.js/discussions/34894
    backendApiUrl: process.env.BACKEND_API_URL || "http://shop-backend",
  },
  publicRuntimeConfig: {
    // Standalone mode resolves all variables at build time
    appName: process.env.APP_NAME || "shop-frontend-browser",
    faroUrl: process.env.FARO_API_URL || "https://telemetry.dev-gcp.nav.cloud.nais.io/collect",
  },
  experimental: {
    // Enable OpenTelemetry instrumentation for Next.js
    // https://nextjs.org/docs/pages/building-your-application/optimizing/open-telemetry
    instrumentationHook: true,
  },
}

module.exports = nextConfig
