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
  experimental: {
    // Enable OpenTelemetry instrumentation for Next.js
    // https://nextjs.org/docs/pages/building-your-application/optimizing/open-telemetry
    instrumentationHook: true,
  },
}

module.exports = nextConfig
