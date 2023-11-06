/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  swcMinify: true,
  serverRuntimeConfig: {
    backendApiUrl: process.env.BACKEND_API_URL,
  },
  experimental: {
    // Enable OpenTelemetry instrumentation for Next.js
    // https://nextjs.org/docs/pages/building-your-application/optimizing/open-telemetry
    instrumentationHook: true,
  },
}

module.exports = nextConfig
