const loadStuffFromApi = async () => {
    const response = await fetch('/api/getstuff')
    if (response.status >= 300) {
        throw `API returned status: ${response.status}`
    }

    return {
        status: response.status,
        data: await response.text(),
    }
}

export default loadStuffFromApi