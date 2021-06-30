import authUtils from '../auth/utils';
import config from '../config';
import proxy from 'express-http-proxy';
import url from 'url';

const options = (authClient) => ({
    parseReqBody: false,
    proxyReqOptDecorator: (options, req) => {
        const clientId = config.reverseProxy.clientId
        const scope = `api://${clientId}/.default`
        return new Promise(((resolve, reject) =>
            authUtils.getOnBehalfOfAccessToken(
                authClient, req, clientId, scope
            ).then(access_token => {
                options.headers.Authorization = `Bearer ${access_token}`;
                resolve(options)
            },
            error => reject(error))
        ))
    },
    proxyReqPathResolver: req => {
        const urlFromApi = url.parse(config.reverseProxy.url);
        const pathFromApi = (urlFromApi.pathname === '/' ? '' : urlFromApi.pathname);

        const urlFromRequest = url.parse(req.originalUrl);
        const pathFromRequest = urlFromRequest.pathname.replace(`/${config.reverseProxy.path}/`, '/');

        const queryString = urlFromRequest.query;
        const newPath = (pathFromApi ? pathFromApi : '') + (pathFromRequest ? pathFromRequest : '') + (queryString ? '?' + queryString : '');

        console.log(`Proxying request from '${req.originalUrl}' to '${stripTrailingSlash(urlFromApi.href)}${newPath}'`);
        return newPath;
    }
});

const stripTrailingSlash = (str) => str.endsWith('/') ? str.slice(0, -1) : str;

const setup = (router, authClient) => {
    router.use(`/${config.reverseProxy.path}/*`, proxy(config.reverseProxy.url, options(authClient)))
};

export default { setup }
