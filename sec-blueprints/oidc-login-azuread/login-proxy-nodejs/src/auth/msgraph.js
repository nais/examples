import axios from 'axios';
import utils from './utils';

const getGraphRequest = (authClient, req, url) => {
    return new Promise(((resolve, reject) => {
        const clientId = 'https://graph.microsoft.com'
        const scope = 'https://graph.microsoft.com/.default';
        utils.getOnBehalfOfAccessToken(authClient, req, clientId, scope)
            .then(accessToken => axios.get(url, { headers: {"Authorization": `Bearer ${accessToken}`} }))
            .then(response => resolve(response.data))
            .catch(err => {
                if (err.response.data) reject(err.response.data)
                else reject(err)
            })
    }))
}

const getUserInfoFromGraphApi = (authClient, req) => {
    const query = 'onPremisesSamAccountName,displayName,givenName,mail,officeLocation,surname,userPrincipalName,id,jobTitle';
    const graphUrl = `https://graph.microsoft.com/v1.0/me?$select=${query}`;
    return getGraphRequest(authClient, req, graphUrl)
};

const getUserGroups = (authClient, req) => {
    const graphUrl = `https://graph.microsoft.com/v1.0/me/memberOf`;
    return getGraphRequest(authClient, req, graphUrl)
};

export default {
    getUserInfoFromGraphApi,
    getUserGroups,
};
