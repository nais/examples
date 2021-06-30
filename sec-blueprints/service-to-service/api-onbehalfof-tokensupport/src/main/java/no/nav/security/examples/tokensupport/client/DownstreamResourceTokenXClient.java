package no.nav.security.examples.tokensupport.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DownstreamResourceTokenXClient {

    private final String url;
    private final RestTemplate restTemplate;

    public DownstreamResourceTokenXClient(@Value("${downstream.resource.url}") String url, @Qualifier("tokenx") RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    public String tokeninfo() {
        return restTemplate.getForObject(url + "/tokenx/tokeninfo", String.class);
    }

    public String ping() {
        return restTemplate.getForObject(url + "/tokenx", String.class);
    }
}
