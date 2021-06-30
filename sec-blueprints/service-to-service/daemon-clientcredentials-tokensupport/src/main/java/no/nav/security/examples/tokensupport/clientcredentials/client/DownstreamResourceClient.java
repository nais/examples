package no.nav.security.examples.tokensupport.clientcredentials.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DownstreamResourceClient {

    private final String url;
    private final RestTemplate restTemplate;

    public DownstreamResourceClient(@Value("${downstream.resource.url}") String url, RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    public String ping() {
        return restTemplate.getForObject(url + "/ping", String.class);
    }
}
