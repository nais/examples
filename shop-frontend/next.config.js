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
    env: process.env.NODE_ENV || "production",
    appName: process.env.APP_NAME || "shop-frontend",
    faroUrl: process.env.FARO_API_URL || "https://telemetry.ekstern.dev.nav.no/collect",
    faroAppName: process.env.FARO_APP_NAME || "shop-frontend-browser",
  },
  //experimental: {
  //  // Enable OpenTelemetry instrumentation for Next.js
  //  // https://nextjs.org/docs/pages/building-your-application/optimizing/open-telemetry
  //  instrumentationHook: true,
  //},
}

module.exports = nextConfig
