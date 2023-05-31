# Next.js with Unleash

Next.js example with Unleash feature flags. This is adopted from the [Unleash Client Next.js example](https://github.com/Unleash/unleash-client-nextjs/tree/main/example).

Create a new feature flag in Unleash with the name `nextjs-example` and enable it for `development` environment.

Create two new API clients; one with type `CLIENT` and one with type `FRONTEND`.

Add the following environment variables to your `.env` file:

```bash
NEXT_PUBLIC_UNLEASH_SERVER_API_URL=https://myteam-unleash-api.nav.cloud.nais.io/api
UNLEASH_SERVER_API_TOKEN=*:development.abc123
NEXT_PUBLIC_UNLEASH_FRONTEND_API_TOKEN=*:development.def456
NEXT_PUBLIC_UNLEASH_APP_NAME=nextjs-example
UNLEASH_RELAY_SECRET=secret
```

Run the application locally:

```bash
git clone https://github.com/nais/examples.git
cd examples/unleash-dings
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser to brows the example application.

Add a variant to the `nextjs-example` feature flag in Unleash, with the name `nextjs-example` and value `a` or `b` for the development environment.

## Available examples

### App Router

- `./src/app/app-page/page.tsx` - Server-side component page, with loader
- `./src/app/api-route/route.tsx` - JSON API response

### Pages Router

- `./src/pages/csr.tsx` - Client-side rendering - simple use case, with loader
- `./src/pages/ssr.tsx` - Server-side rendering - when you need to keep some data private
- `./src/pages/ssg.tsx` - Static site generation - performance optimization

### API

- `./src/pages/api/hello.ts` - API route responding with JSON

### Middleware

Example of A/B testing with Next.js Middleware.
Redirect users to a different (pre-rendered) page based on a feature flag.

- `./src/pages/api/proxy-definitions.ts` - act as cache for feature flag definitions. This lets this SDK act as a substitute for Unleash Edge or the Unleash proxy that you can deploy on Next.js Edge.
- `./src/middleware.ts`- handle flag evaluation and transparently redirect to one of the pages in `./src/pages/ab` directory
- `./src/pages/ab/a` & `./src/pages/ab/b` - target pages. Both will be served at the URL `/ab`, but which one you see is decided by the feature flag in `./src/middleware.ts`.
