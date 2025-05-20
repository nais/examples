module.exports = {
  output: 'standalone',
  reactStrictMode: true,
  serverExternalPackages: ['@navikt/next-logger', 'next-logger', 'pino', 'pino-roll'],
  experimental: {
    optimizePackageImports: ['@navikt/ds-react', '@navikt/aksel-icons'],
    authInterrupts: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'fonts.googleapis.com',
        pathname: '/css2/**',
      },
    ],
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Content-Security-Policy',
            value: "font-src 'self' https://fonts.googleapis.com;",
          },
        ],
      },
    ];
  },
  publicRuntimeConfig: {
    githubUrl: 'https://github.com/nais/examples/tree/main/quotes-frontend',
    grafanaLogsUrl: "https://grafana.nav.cloud.nais.io/a/grafana-lokiexplore-app/explore/service/quotes-frontend/logs?from=now-1h&to=now&var-ds=P7BE696147D279490&var-filters=service_name%7C%3D%7Cquotes-frontend",
    grafanaDashboardUrl: "https://grafana.nav.cloud.nais.io/d/eemcfslr8f94wb/nais-quotes?orgId=1&from=now-3h&to=now&timezone=browser&var-Filters="
  },
  env: {
    NEXT_PUBLIC_BACKEND_URL: process.env.NEXT_PUBLIC_BACKEND_URL || 'http://quotes-backend',
  },
};
