import {Issuer, TokenSet} from 'openid-client'
import logger from 'winston-logstash-format'

let tokenxConfig = null
let tokenxClient = null
let tokenxMetadata = null
let idportenConfig = null
let idportenClient = null
let idportenMetadata = null
let appConfig = null

export const setup = async (idpConfig, txConfig, appConf) => {
    idportenConfig = idpConfig
    tokenxConfig = txConfig
    appConfig = appConf
    return init().then((clients) => {
        idportenClient = clients.idporten
        tokenxClient = clients.tokenx
    })
}

export const authUrl = (session) => {
    return idportenClient.authorizationUrl({
        scope: idportenConfig.scope,
        redirect_uri: idportenConfig.redirectUri,
        response_type: 'code',
        response_mode: 'query',
        nonce: session.nonce,
        state: session.state,
        resource: "https://nav.no",
        acr_values: "Level4",
    })
}

export const validateOidcCallback = async (req) => {
    const params = idportenClient.callbackParams(req)
    const nonce = req.session.nonce
    const state = req.session.state
    const additionalClaims = {
        clientAssertionPayload: {
            aud: idportenMetadata.metadata.issuer
        }
    }

    return idportenClient
        .callback(idportenConfig.redirectUri, params, {nonce, state}, additionalClaims)
        .catch((err) => Promise.reject(`error in oidc callback: ${err}`))
        .then(async (tokenSet) => {
            return tokenSet
        })
}

export const exchangeToken = async (session, servicename) => {
    const cachedToken = cachedTokenFrom(session, servicename)
    if (cachedToken) {
        logger.debug(`Using cached token for ${servicename}`)
        return Promise.resolve(cachedToken)
    }

    // additional claims not set by openid-client
    const additionalClaims = {
        clientAssertionPayload: {
            'nbf': Math.floor(Date.now() / 1000),
            // TokenX only allows a single audience
            'aud': [ tokenxMetadata.token_endpoint ],
        }
    }

    return tokenxClient.grant({
        grant_type: 'urn:ietf:params:oauth:grant-type:token-exchange',
        client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
        subject_token_type: 'urn:ietf:params:oauth:token-type:jwt',
        audience: appConfig.targetAudience,
        subject_token: session.tokens.access_token
    }, additionalClaims).then(tokenSet => {
        logger.debug(`Retrieved new token for ${servicename}`)
        session[`${servicename}_tokenset`] = tokenSet
        return Promise.resolve(tokenSet.access_token)
    }).catch(err => {
        logger.error(`Error while exchanging token: ${err}`)
        return Promise.reject(err)
    })

}

export const refresh = (oldTokenSet) => {
    const additionalClaims = {
        clientAssertionPayload: {
            'aud': idportenMetadata.issuer
        }
    }
    return idportenClient.refresh(new TokenSet(oldTokenSet), additionalClaims)
}

const init = async () => {
    const idporten = await Issuer.discover(idportenConfig.discoveryUrl)
    const tokenx = await Issuer.discover(tokenxConfig.discoveryUrl)
    tokenxMetadata = tokenx
    idportenMetadata = idporten
    logger.info(`discovered idporten @ ${idporten.issuer}`)
    logger.info(`discovered tokenx @ ${tokenx.issuer}`)
    try {
        const idportenJwk = JSON.parse(idportenConfig.clientJwk)
        const tokenxJwk = JSON.parse(tokenxConfig.privateJwk)
        idportenClient = new idporten.Client({
            client_id: idportenConfig.clientID,
            token_endpoint_auth_method: 'private_key_jwt',
            token_endpoint_auth_signing_alg: 'RS256',
            redirect_uris: [idportenConfig.redirectUri, 'http://localhost:3000/callback'],
            response_types: ['code']
        }, {
            keys: [idportenJwk]
        })

        tokenxClient = new tokenx.Client({
            client_id: tokenxConfig.clientID,
            token_endpoint_auth_method: 'private_key_jwt'
        }, {
            keys: [tokenxJwk]
        })

        return Promise.resolve({idporten: idportenClient, tokenx: tokenxClient})
    } catch (err) {
        return Promise.reject(err)
    }
}

const cachedTokenFrom = (session, servicename) => {
    const raw = session[`${servicename}_tokenset`]
    if (raw) {
        const tokenSet = new TokenSet(raw);
        if (!tokenSet.expired()) {
            return tokenSet.access_token
        }
    }
    return null;
}


