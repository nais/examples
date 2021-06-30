import 'dotenv/config';

const envVar = ({name, required = true}) => {
    if (!process.env[name] && required) {
        console.error(`Missing required environment variable '${name}'`);
        process.exit(1);
    }
    return process.env[name]
};

const server = {
    // should be equivalent to the URL this application is hosted on for correct CORS origin header
    host: envVar({name: "HOST", required: false}) || 'localhost',

    // port for your application
    port: envVar({name: "PORT", required: false}) || 3000,

    // optional, only set if requests to Azure AD must be performed through a corporate proxy (i.e. traffic to login.microsoftonline.com is blocked by the firewall)
    proxy: envVar({name: "HTTP_PROXY", required: false}),

    // should be set to a random key of significant length for signing session ID cookies
    sessionKey: envVar({name: "SESSION_KEY", required: true}),

    // name of the cookie, set to whatever your want
    cookieName: 'security-blueprints-login'
};

const azureAd = {
    // automatically provided by NAIS at runtime
    discoveryUrl: envVar({name: "AZURE_APP_WELL_KNOWN_URL", required: true}),
    clientId: envVar({name: "AZURE_APP_CLIENT_ID", required: true}),
    clientJwks: JSON.parse(envVar({name: "AZURE_APP_JWKS", required: true})),

    // not provided by NAIS, must be configured
    // where the user should be redirected after authenticating at the third party
    // should be "$host + /oauth2/callback", e.g. http://localhost:3000/oauth2/callback
    redirectUri: envVar({name: "AZURE_APP_REDIRECT_URL", required: true}),

    // not provided by NAIS, must be configured
    // where your application should redirect the user after logout
    logoutRedirectUri: envVar({name: "AZURE_APP_LOGOUT_REDIRECT_URL", required: true}),

    // leave at default
    tokenEndpointAuthMethod: 'private_key_jwt',
    responseTypes: ['code'],
    responseMode: 'query',
    tokenEndpointAuthSigningAlg: 'RS256',
};

const redis = {
    host: envVar({name: "REDIS_HOST", required: false}),
    port: envVar({name: "REDIS_PORT", required: false}) || 6379,
    password: envVar({name: "REDIS_PASSWORD", required: false})
};

const reverseProxy = {
    clientId: envVar({name: "DOWNSTREAM_API_CLIENT_ID"}),
    path: envVar({name: "DOWNSTREAM_API_PATH", required: false}) || 'downstream',
    url: envVar({name: "DOWNSTREAM_API_URL"})
};

export default {
    server,
    azureAd,
    reverseProxy,
    redis
};
