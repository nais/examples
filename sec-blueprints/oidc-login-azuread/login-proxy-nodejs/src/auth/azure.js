import {custom, Issuer, Strategy} from 'openid-client';
import authUtils from './utils';
import config from '../config';
import httpProxy from '../proxy/http-proxy';

const metadata = {
    client_id: config.azureAd.clientId,
    redirect_uris: [config.azureAd.redirectUri],
    token_endpoint_auth_method: config.azureAd.tokenEndpointAuthMethod,
    token_endpoint_auth_signing_alg: config.azureAd.tokenEndpointAuthSigningAlg,
};

const client = async () => {
    if (httpProxy.agent) {
        custom.setHttpOptionsDefaults({
            agent: httpProxy.agent
        });
    }
    const issuer = await Issuer.discover(config.azureAd.discoveryUrl);
    console.log(`Discovered issuer ${issuer.issuer}`);
    const jwks = config.azureAd.clientJwks
    return new issuer.Client(metadata, jwks);
};

const strategy = client => {
    const verify = (tokenSet, done) => {
        if (tokenSet.expired()) {
            return done(null, false)
        }
        const user = {
            'tokenSets': {
                [authUtils.tokenSetSelfId]: tokenSet
            },
            'claims': tokenSet.claims()
        };
        return done(null, user);
    };
    const options = {
        client: client,
        params: {
            response_types: config.azureAd.responseTypes,
            response_mode: config.azureAd.responseMode,
            scope: `openid ${config.azureAd.clientId}/.default`
        },
        extras: { clientAssertionPayload: { aud: client.issuer.metadata.token_endpoint }},
        passReqToCallback: false,
        usePKCE: 'S256'
    };
    return new Strategy(options, verify);
};

export default { client, strategy };
