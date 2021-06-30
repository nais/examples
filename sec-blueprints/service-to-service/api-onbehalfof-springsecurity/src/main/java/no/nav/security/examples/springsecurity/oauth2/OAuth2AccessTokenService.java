package no.nav.security.examples.springsecurity.oauth2;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

/**
 * Not used in code right now, but left in to show an
 * alternative to using Springs built-in support (somewhat complicated) for managing clients etc.
 */
public class OAuth2AccessTokenService {

    private final AuthenticationTokenResolver authenticationTokenResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> onBehalfOfClient;

    public OAuth2AccessTokenService(AuthenticationTokenResolver authenticationTokenResolver,
                                    ClientRegistrationRepository clientRegistrationRepository,
                                    OAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> onBehalfOfClient) {

        this.authenticationTokenResolver = authenticationTokenResolver;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.onBehalfOfClient = onBehalfOfClient;
    }

    public OAuth2AccessTokenResponse getOnBehalfOfToken(String clientRegistrationId) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        String accessToken = authenticationTokenResolver
                .jwtAuthenticationToken()
                .map(AbstractOAuth2TokenAuthenticationToken::getToken)
                .map(AbstractOAuth2Token::getTokenValue)
                .orElseThrow(() -> new RuntimeException("no authenticated access_token found, cannot do on-behalf-of"));

        return onBehalfOfClient.getTokenResponse(new OAuth2JwtBearerGrantRequest(clientRegistration, accessToken));
    }
}
