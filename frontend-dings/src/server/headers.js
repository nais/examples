const cspString = `default-src 'self'; upgrade-insecure-requests; block-all-mixed-content; base-uri; plugin-types`

export const setup = (app) => {
    app.disable('x-powered-by')
    app.use((req, res, next) => {
        res.header('X-Frame-Options', 'DENY')
        res.header('X-Xss-Protection', '1; mode=block')
        res.header('X-Content-Type-Options', 'nosniff')
        res.header('Referrer-Policy', 'no-referrer')

        res.header('Content-Security-Policy', cspString)
        res.header('X-WebKit-CSP', cspString)
        res.header('X-Content-Security-Policy', cspString)

        res.header('Feature-Policy', "geolocation 'none'; microphone 'none'; camera 'none'")
        if (process.env.NODE_ENV === 'development') {
            res.header('Access-Control-Allow-Origin', 'http://localhost:1234')
            res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
            res.header('Access-Control-Allow-Methods', 'GET, POST')
        }
        next()
    })
}
