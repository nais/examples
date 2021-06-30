package no.nav.security.examples.springsecurity.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
class ProtectedResourceController {

    @GetMapping("tokeninfo")
    Map<String, Object> tokenInfo(@AuthenticationPrincipal JwtAuthenticationToken jwtAuthenticationToken) {
        return jwtAuthenticationToken.getToken().getClaims();
    }

    @GetMapping
    String ping() {
        return "end of the line";
    }
}
