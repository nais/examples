'user strict'

import { setup } from './telemetry'

// Require in rest of modules
import express, { type Express } from 'express'
import { authMiddleware } from './middleware'
import { getCatsController } from './routes'
import axios from 'axios'
setup('example-express-server')

// Setup express
const app: Express = express()
const PORT = 8080

app.use(express.json())
app.get('/health', (_req, res) => {
  res.status(200).send('HEALTHY')
})
app.get('/run_test', (_req, res) => {
  // Calls another endpoint of the same API, somewhat mimicking an external API call
  axios.post(
    `http://localhost:${PORT}/cats`,
    {
      name: 'Tom',
      friends: ['Jerry']
    },
    {
      headers: {
        Authorization: 'secret_token'
      }
    }
  ).then((createdCat) => {
    res.status(201).send(createdCat.data)
  }).catch((error) => {
    console.error(error)
    res.sendStatus(500)
  })
})

app.use('/cats', authMiddleware, getCatsController())

app.listen(PORT, () => {
  console.log(`Application listening on http://localhost:${PORT}`)
})
