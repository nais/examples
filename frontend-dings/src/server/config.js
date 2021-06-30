import dotenv from 'dotenv'

dotenv.config()

export const app = {
    useSecureCookies: !!process.env.NAIS_CLUSTER_NAME,
    port: process.env.PORT || 3000,
    apidingsUrl: process.env.API_DINGS_URL || 'https://api-dings.dev-fss-pub.nais.io',
    targetAudience: process.env.API_DINGS_AUDIENCE || 'dev-fss:plattformsikkerhet:api-dings',
}

export const session = {
    secret: process.env.SESSION_SECRET,
    maxAgeMs: process.env.SESSION_MAX_AGE_MS || 2 * 60 * 60 * 1000, // defaults to 2 hours
    redisHost: process.env.REDIS_HOST,
    redisPort: process.env.REDIS_PORT || 6379,
    redisPassword: process.env.REDIS_PASSWORD,
}

export const idporten = {
    discoveryUrl: process.env.IDPORTEN_WELL_KNOWN_URL || "https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration",
    clientID: process.env.IDPORTEN_CLIENT_ID,
    clientJwk: process.env.IDPORTEN_CLIENT_JWK,
    redirectUri : process.env.IDPORTEN_REDIRECT_URI || "http://localhost:3000/oauth2/callback",
    responseType: ['code'],
    scope: 'openid profile',
}

export const tokenx = {
    discoveryUrl: process.env.TOKEN_X_WELL_KNOWN_URL,
    clientID: process.env.TOKEN_X_CLIENT_ID,
    privateJwk: process.env.TOKEN_X_PRIVATE_JWK
}

