package no.nav.security.examples.tokensupport.rest;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
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
    private final TokenValidationContextHolder tokenValidationContextHolder;

    ProtectedResourceController(TokenValidationContextHolder tokenValidationContextHolder) {
        this.tokenValidationContextHolder = tokenValidationContextHolder;
    }

    @GetMapping
    String ping() {
        return "end of the line";
    }

    @GetMapping("tokeninfo")
    Map<String, Object> tokenInfo() {
        return Optional.of(tokenValidationContextHolder.getTokenValidationContext())
                .map(ctx -> ctx.getJwtToken(ISSUER_AAD))
                .map(JwtToken::getJwtTokenClaims)
                .map(JwtTokenClaims::getAllClaims)
                .orElse(null);
    }
}
