package no.nav.security.examples.springsecurity.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DownstreamResourceClient {

    private static final String URL_KEY = "${clients.example-clientcredentials.url}";
    private final String url;
    private final RestTemplate restTemplate;

    public DownstreamResourceClient(@Value(URL_KEY) String url, RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    public String ping() {
        return restTemplate.getForObject(url + "/downstream/ping", String.class);
    }
}
