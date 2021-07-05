import session from "express-session"
import redis from "redis"
import * as config from "./config.js"
import RedisStore from "connect-redis"

export const setupSession = () => {
    try {
        const options = {
            cookie: {
                maxAge: config.session.maxAgeMs,
                sameSite: 'lax',
                httpOnly: true,
            },
            secret: config.session.secret,
            name: 'frontend-dings',
            resave: false,
            saveUninitialized: false,
            unset: 'destroy',
        }
        if (process.env.NODE_ENV !== 'development') {
            options.cookie.secure = true
            options.store = setupRedis()
        }
        return session(options)   
    } catch (error) {
        console.error(error)
    }
}

const setupRedis = () => {
    console.log(`redis host: ${config.session.redisHost}`)
    if (!config.session.redisPassword || config.session.redisPassword.trim().length === 0) {
        console.error('redis password is missing')
    }
    const store = RedisStore(session)
    const client = redis.createClient({
        host: config.session.redisHost,
        password: config.session.redisPassword,
        port: config.session.redisPort,
    })
    client.unref()
    client.on('debug', console.log)

    return new store({
        client: client,
        disableTouch: true,
    })
}
