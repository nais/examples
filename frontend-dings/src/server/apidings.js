import fetch from 'node-fetch'

let baseUrl = null

export const init = (apidingsUrl) => {
    baseUrl = apidingsUrl
}

export const getStuff = async (bearerToken) => {
    return fetch(`${baseUrl}/hello`, {
        method: 'get',
        headers: {"Authorization": `Bearer ${bearerToken}`}
    }).then(res => res.text())
}

