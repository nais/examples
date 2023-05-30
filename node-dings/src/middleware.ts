'use strict'

import { type RequestHandler } from 'express'
import opentelemetry from '@opentelemetry/api'

const meter = opentelemetry.metrics.getMeter('app.middleware')
const authFailuresCounter = meter.createCounter('auth.failures', {
  description: 'Authorization failures'
})

export const authMiddleware: RequestHandler = (req, res, next) => {
  const { authorization } = req.headers
  if (authorization?.includes('secret_token') ?? false) {
    next()
  } else {
    authFailuresCounter.add(1)
    res.sendStatus(401)
  }
}
