package no.nav.security.examples.springsecurity.rest;

import com.fasterxml.jackson.databind.JsonNode;
import no.nav.security.examples.springsecurity.client.DownstreamResourceClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
class ProtectedResourceController {

    private static final String DOWNSTREAM_BASE_PATH = "/downstream/api";
    private final DownstreamResourceClient downstreamResourceClient;

    ProtectedResourceController(DownstreamResourceClient downstreamResourceClient) {
        this.downstreamResourceClient = downstreamResourceClient;
    }

    @GetMapping("/tokeninfo")
    Map<String, Object> tokenInfo(@AuthenticationPrincipal JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getToken().getClaims();
    }

    @GetMapping
    String ping() {
        return "you have reached a middle-tier secured api.";
    }

    @GetMapping(DOWNSTREAM_BASE_PATH)
    String pingSecuredDownstreamApi() {
        return downstreamResourceClient.ping();
    }

    @GetMapping(path = DOWNSTREAM_BASE_PATH + "/tokeninfo", produces= MediaType.APPLICATION_JSON_VALUE)
    String tokenInfoDownstreamApi() {
        return downstreamResourceClient.tokeninfo();
    }
}
