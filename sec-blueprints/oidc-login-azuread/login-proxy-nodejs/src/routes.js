import authUtils from './auth/utils';
import config from './config';
import express from 'express';
import passport from 'passport';
import path from 'path';
import session from 'express-session';
import reverseProxy from "./proxy/reverse-proxy";
import msgraph from "./auth/msgraph";

const router = express.Router();

const ensureAuthenticated = async (req, res, next) => {
    if (req.isAuthenticated() && authUtils.hasValidAccessToken(req)) {
        next();
    } else {
        session.redirectTo = req.url;
        res.redirect('/login');
    }
};

const setup = authClient => {
    // These routes are unprotected and do not require auth to reach

    // Liveness and readiness probes for Kubernetes / nais
    router.get('/isAlive', (req, res) => res.send('Alive'));
    router.get('/isReady', (req, res) => res.send('Ready'));

    // Routes for passport to handle the authentication flow
    router.get('/login', passport.authenticate('azureOidc', { failureRedirect: '/login'}));
    router.use('/oauth2/callback', passport.authenticate('azureOidc', { failureRedirect: '/login'}), (req, res) => {
        if (session.redirectTo) {
            res.redirect(session.redirectTo);
        } else {
            res.redirect('/');
        }
    });

    // The routes below require the user to be authenticated
    router.use(ensureAuthenticated);

    // return tokeninfo for the current user
    // DO NOT DO THIS IN PRODUCTION
    router.get('/', (req, res) => {
        res.json(req.user);
    });

    // return user info fetched from the Microsoft Graph API
    router.get('/me', (req, res) => {
        msgraph.getUserInfoFromGraphApi(authClient, req)
            .then(userinfo => res.json(userinfo))
            .catch(err => res.status(500).json(err))
    });

    // return groups that the user is a member of from the Microsoft Graph API
    router.get('/me/memberOf', (req, res) => {
        msgraph.getUserGroups(authClient, req)
            .then(userinfo => res.json(userinfo))
            .catch(err => res.status(500).json(err))
    });

    // log the user out
    router.get('/logout', (req, res) => {
        req.logOut();
        res.redirect(authClient.endSessionUrl({ 'post_logout_redirect_uri': config.azureAd.logoutRedirectUri }));
    });

    // set up reverse proxy for calling APIs/backends using the on-behalf-of flow
    reverseProxy.setup(router, authClient);

    // serve static files
    router.use(express.static(path.join(__dirname, '../website/production')));
    router.use('*', (req, res) => {
        res.sendFile('index.html', { root: path.join(__dirname, '../website/production') });
    });

    return router;
};

export default { setup };
