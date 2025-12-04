module.exports = {
  output: 'standalone',
  reactStrictMode: true,
  serverExternalPackages: ['@navikt/next-logger', 'next-logger', 'pino', 'pino-roll'],
  trailingSlash: false,
  generateBuildId: () => 'build',
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
  env: {
    NEXT_PUBLIC_BACKEND_URL: process.env.NEXT_PUBLIC_BACKEND_URL || 'http://quotes-backend',
    NEXT_PUBLIC_GITHUB_URL: process.env.NEXT_PUBLIC_GITHUB_URL || 'https://github.com/nais/examples/tree/main/quotes-frontend',
    NEXT_PUBLIC_GRAFANA_DASHBOARD_URL: process.env.NEXT_PUBLIC_GRAFANA_DASHBOARD_URL || 'https://grafana.nav.cloud.nais.io/d/eemcfslr8f94wb/nais-quotes?orgId=1&from=now-3h&to=now&timezone=browser&var-Filters=',
    ANALYTICS_SERVICE_URL: process.env.ANALYTICS_SERVICE_URL || 'http://quotes-analytics',
  },
};
