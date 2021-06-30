import express from 'express'
import  { generators, TokenSet }  from 'openid-client'
import logger from 'winston-logstash-format'
import * as auth from './auth.js'
import * as config from './config.js'
import * as headers from './headers.js'
import * as apidings from './apidings.js'
import { limit } from'./ratelimit.js'
import { setupSession } from './session.js'

const app = express()

let authEndpoint = null
auth.setup(config.idporten, config.tokenx, config.app).then((endpoint) => {
    authEndpoint = endpoint
}).catch((err) => {
    logger.error(`Error while setting up auth: ${err}`)
    process.exit(1)
})

app.use(express.text())
headers.setup(app)
apidings.init(config.app.apidingsUrl)

app.use(limit)

app.set('trust proxy', 1)
app.use(setupSession())

app.get(['/internal/isalive', '/internal/isready'], async (req, res) => {
  res.sendStatus(200)
})

app.get("/login", async (req, res) => { // lgtm [js/missing-rate-limiting]
  const session = req.session
  session.nonce = generators.nonce()
  session.state = generators.state()
  res.redirect(auth.authUrl(session))
})

app.get("/oauth2/callback", async (req, res) => {
  const session = req.session
  auth.validateOidcCallback(req)
      .then((tokens) => {
         session.tokens = tokens
         session.state = null
         session.nonce = null
          res.cookie('dings-id', `${tokens.id_token}`, {
              secure: config.app.useSecureCookies,
              sameSite: "lax",
              maxAge: config.session.maxAgeMs
          })
          res.redirect(303, '/')
      })
      .catch((err) => {
          logger.error(err)
          session.destroy(() => {})
          res.sendStatus(403)
      })
})

// check auth
app.use(async (req, res, next) => {
    let currentTokens = req.session.tokens
    if (!currentTokens) {
        res.redirect("/login")
    } else {
        const currentTokenSet = new TokenSet(currentTokens)
        if (currentTokenSet.expired()) {
            logger.debug("refreshing token")
            auth
                .refresh(currentTokens)
                .then((refreshedTokenSet) => {
                    req.session.tokens = new TokenSet(refreshedTokenSet)
                })
                .catch((err) => {
                    logger.error(err)
                    req.session.destroy()
                    res.redirect('/login')
                })
        }
        return next()
    }
})

// authenticated routes below
app.get('/api/getstuff', async (req, res) => {
  try {
    const accessToken = await auth.exchangeToken(req.session, 'apidings')
    const response = await apidings.getStuff(accessToken)
    res.send(response)
  } catch (err) {
    logger.error(`Error while calling api: ${err}`)
    res.sendStatus(500)
  }
})

app.use(express.static('dist/client'))

app.listen(config.app.port, () => {
  logger.info(`frontend-dings listening at port ${config.app.port}`)
})
