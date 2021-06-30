import azure from './auth/azure';
import config from './config';
import routes from './routes';
import cors from 'cors';
import express from 'express';
import helmet from 'helmet';
import passport from 'passport';
import session from './session';
import limit from './ratelimit';

// for debugging during development
import morganBody from 'morgan-body';
import morgan from 'morgan';

const server = express();
const port = config.server.port;

async function startApp()  {
    try {
        morganBody(server);
        morgan('dev');

        session.setup(server);

        server.use(express.json());
        server.use(express.urlencoded({ extended: true }));
        server.use(limit);

        // setup defaults for CORS and HTTP headers, adjust as needed
        server.use(helmet());
        server.use(cors({
            origin: config.server.host,
            methods: ['GET'],
            exposedHeaders: ['Origin', 'Content-Type', 'Accept', 'X-Requested-With'],
        }));

        // initialize passport and restore authentication state, if any, from the session
        server.use(passport.initialize());
        server.use(passport.session());

        const azureAuthClient = await azure.client();
        const azureOidcStrategy = azure.strategy(azureAuthClient);

        passport.use('azureOidc', azureOidcStrategy);
        passport.serializeUser((user, done) => done(null, user));
        passport.deserializeUser((user, done) => done(null, user));

        // setup routes
        server.use('/', routes.setup(azureAuthClient));

        server.listen(port, () => console.log(`Listening on port ${port}`));
    } catch (error) {
        console.error('Error during start-up', error);
    }
}

startApp().catch(err => console.log(err));
