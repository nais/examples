import config from '../config';
import {HttpsProxyAgent} from "https-proxy-agent";

const agent = () => {
    const proxyUri = config.server.proxy;
    if (proxyUri) {
        console.log(`Proxying requests via ${proxyUri} for openid-client`);
        const agent = new HttpsProxyAgent(proxyUri);
        return {
            http: agent,
            https: agent,
        }
    } else {
        console.log(`Environment variable HTTP_PROXY is not set, not proxying requests for openid-client`);
        return null
    }
};

export default { agent: agent() }
