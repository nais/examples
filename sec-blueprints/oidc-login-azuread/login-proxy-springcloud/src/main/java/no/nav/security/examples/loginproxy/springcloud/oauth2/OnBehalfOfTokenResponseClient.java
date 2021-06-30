package no.nav.security.examples.loginproxy.springcloud.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.security.oauth2.core.web.reactive.function.OAuth2BodyExtractors.oauth2AccessTokenResponse;

@Component
@Slf4j
public class OnBehalfOfTokenResponseClient implements ReactiveOAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> {

    private static final String OAUTH2_PARAMETER_NAME_ASSERTION = "assertion";
    private static final String OAUTH2_PARAMETER_NAME_REQUESTED_TOKEN_USE = "requested_token_use";
    private final WebClient webClient;

    public OnBehalfOfTokenResponseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<OAuth2AccessTokenResponse> getTokenResponse(OAuth2JwtBearerGrantRequest grantRequest) {
        Assert.notNull(grantRequest, "grantRequest cannot be null");
        return Mono.defer(() -> this.webClient.post()
                .uri(grantRequest.getClientRegistration().getProviderDetails().getTokenUri())
                .headers(headers -> populateTokenRequestHeaders(grantRequest, headers))
                .body(createTokenRequestBody(grantRequest))
                .exchange()
                .flatMap(response -> response.body(oauth2AccessTokenResponse())));// readTokenResponse(grantRequest, response)));
    }

    private void populateTokenRequestHeaders(OAuth2JwtBearerGrantRequest grantRequest, HttpHeaders headers) {
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
        }
    }

    private BodyInserters.FormInserter<String> createTokenRequestBody(OAuth2JwtBearerGrantRequest grantRequest) {
        BodyInserters.FormInserter<String> body = BodyInserters
                .fromFormData(OAuth2ParameterNames.GRANT_TYPE, grantRequest.getGrantType().getValue());
        return populateTokenRequestBody(grantRequest, body);
    }

    BodyInserters.FormInserter<String> populateTokenRequestBody(OAuth2JwtBearerGrantRequest grantRequest, BodyInserters.FormInserter<String> body) {
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();
        if (!ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            body.with(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
        }
        if (ClientAuthenticationMethod.POST.equals(clientRegistration.getClientAuthenticationMethod())) {
            body.with(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }

        String scope = String.join(" ",
                Optional.ofNullable(clientRegistration.getScopes())
                        .orElseThrow(() ->
                                new RuntimeException("scope must be set for client with registrationId="
                                        + clientRegistration.getRegistrationId())));

        body.with(OAuth2ParameterNames.SCOPE, scope);
        body.with(OAUTH2_PARAMETER_NAME_ASSERTION, grantRequest.getAssertion());
        body.with(OAUTH2_PARAMETER_NAME_REQUESTED_TOKEN_USE, "on_behalf_of");
        return body;
    }
}
