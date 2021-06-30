import { TokenSet } from "openid-client";

const tokenSetSelfId = "self";

const getOnBehalfOfAccessToken = (authClient, req, clientId, scope) => {
    return new Promise(((resolve, reject) => {
        if (hasValidAccessToken(req, clientId)) {
            const tokenSets = getTokenSetsFromSession(req);
            resolve(tokenSets[clientId].access_token);
        } else {
            authClient.grant({
                grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
                requested_token_use: 'on_behalf_of',
                scope: scope,
                assertion: req.user.tokenSets[tokenSetSelfId].access_token
            }, { clientAssertionPayload: { aud: authClient.issuer.metadata.token_endpoint }}).then(tokenSet => {
                req.user.tokenSets[clientId] = tokenSet;
                resolve(tokenSet.access_token);
            }).catch(err => {
                console.error(err);
                reject(err);
            })
        }
    }));
};



const getTokenSetsFromSession = (req) => {
    if (req && req.user) {
        return req.user.tokenSets;
    }
    return null;
};

const hasValidAccessToken = (req, key = tokenSetSelfId) => {
    const tokenSets = getTokenSetsFromSession(req);
    if (!tokenSets) {
        return false;
    }
    const tokenSet = tokenSets[key];
    if (!tokenSet) {
        return false;
    }
    return new TokenSet(tokenSet).expired() === false;
};

export default {
    getOnBehalfOfAccessToken,
    hasValidAccessToken,
    tokenSetSelfId,
};
