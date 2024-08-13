/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  swcMinify: true,
  serverRuntimeConfig: {
    // Standalone mode resolves all variables at build time
    // https://github.com/vercel/next.js/discussions/34894
    flakyServiceUrl: process.env.FLAKY_SERVICE_URL || "http://flaky-service",
  },
  env: {
    // Standalone mode resolves all variables at build time
    nodeEnv: process.env.NODE_ENV || "production",
    appName: process.env.APP_NAME || "flaky-frontend",
    faroUrl: process.env.FARO_API_URL || "https://telemetry.ekstern.dev.nav.no/collect",
    faroAppName: process.env.FARO_APP_NAME || "flaky-frontend-browser",
    faroNamespace: process.env.FARO_NAMESPACE || "examples",
  },
};

export default nextConfig;
