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
};
