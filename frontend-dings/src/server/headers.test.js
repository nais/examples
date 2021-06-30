import express from'express'
import * as headers from './headers'
import * as fetch from 'node-fetch'

let server = null
const port = 5555

beforeAll(() => {
    startServer()
  })
  
  afterAll(() => {
    stopServer()
  })

test('security headers should have been added', async () => {
    const response = await fetch(`http://localhost:${port}/`)
    const selectedHeaders = ['content-security-policy', 'x-xss-protection']
    const actualHeaders = response.headers
    selectedHeaders.forEach((headerName) => expect(actualHeaders.get(headerName)).toBeTruthy())
})

test('x-powered-by header should have been removed', async () => {
    const response = await fetch(`http://localhost:${port}/`)
    expect(response.headers.get('x-powered-by')).toBeFalsy()
})

const startServer = () => {
    const app = express()
    headers.setup(app)

    app.get('/', async (req, res) => res.send('hello'))

    server = app.listen(port, () => {})
}

const stopServer = () => {
    server.close()
}