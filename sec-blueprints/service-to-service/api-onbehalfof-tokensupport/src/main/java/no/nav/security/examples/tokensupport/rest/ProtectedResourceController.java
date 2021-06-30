package no.nav.security.examples.tokensupport.rest;

import no.nav.security.examples.tokensupport.client.DownstreamResourceClient;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@ProtectedWithClaims(issuer = ProtectedResourceController.ISSUER_AAD)
class ProtectedResourceController {
    static final String ISSUER_AAD = "aad";
    private static final String DOWNSTREAM_BASE_PATH = "/downstream/api";
    private final TokenValidationContextHolder tokenValidationContextHolder;
    private final DownstreamResourceClient downstreamResourceClient;

    ProtectedResourceController(TokenValidationContextHolder tokenValidationContextHolder,
                                DownstreamResourceClient downstreamResourceClient) {
        this.tokenValidationContextHolder = tokenValidationContextHolder;
        this.downstreamResourceClient = downstreamResourceClient;
    }

    @GetMapping
    String ping() {
        return "you have reached a middle-tier secured api.";
    }

    @GetMapping("/tokeninfo")
    Map<String, Object> tokenInfo() {
        return Optional.of(tokenValidationContextHolder.getTokenValidationContext())
                .map(ctx -> ctx.getJwtToken(ISSUER_AAD))
                .map(JwtToken::getJwtTokenClaims)
                .map(JwtTokenClaims::getAllClaims)
                .orElse(null);
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
