import extractPersonId from './tokeninterpreter'

test('single cookie present', () => {
    const subject = extractPersonId(singleCookie)
    expect(subject).toEqual('12345678910')
})

test('multiple cookies present', () => {
    const subject = extractPersonId(multipleCookies)
    expect(subject).toEqual('12345678910')
})

const singleCookie = 'dings-id=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsb25nLWlkLWxpa2UtdXVpZCIsIm5hbWUiOiJKb2huIERvZSIsImlhdCI6MTUxNjIzOTAyMiwicGlkIjoiMTIzNDU2Nzg5MTAifQ.7UytsYd8hn31qqpgaaEKsPFR7z5oCurxnnDV0e9r8cc'
const multipleCookies = 'dings-id=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsb25nLWlkLWxpa2UtdXVpZCIsIm5hbWUiOiJKb2huIERvZSIsImlhdCI6MTUxNjIzOTAyMiwicGlkIjoiMTIzNDU2Nzg5MTAifQ.7UytsYd8hn31qqpgaaEKsPFR7z5oCurxnnDV0e9r8cc; tullecookie=whatever'

