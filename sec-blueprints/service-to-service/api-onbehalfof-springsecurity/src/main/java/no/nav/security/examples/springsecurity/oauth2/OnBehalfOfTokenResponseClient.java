package no.nav.security.examples.springsecurity.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
@Slf4j
public class OnBehalfOfTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> {

    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";
    private static final String OAUTH2_PARAMETER_NAME_ASSERTION = "assertion";
    private static final String OAUTH2_PARAMETER_NAME_REQUESTED_TOKEN_USE = "requested_token_use";
    private final RestTemplate restTemplate;

    public OnBehalfOfTokenResponseClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter()));
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2JwtBearerGrantRequest oAuth2JwtBearerGrantRequest) {
        Assert.notNull(oAuth2JwtBearerGrantRequest, "oAuth2JwtBearerGrantRequest cannot be null");
        RequestEntity<?> request = convert(oAuth2JwtBearerGrantRequest);
        try {
            return restTemplate.exchange(request, OAuth2AccessTokenResponse.class).getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("received status code={}, and body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                    ex.getResponseBodyAsString(), request.getUrl().toString());
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }

    private RequestEntity<?> convert(OAuth2JwtBearerGrantRequest oAuth2JwtBearerGrantRequest) {
        HttpHeaders headers = getTokenRequestHeaders(oAuth2JwtBearerGrantRequest.getClientRegistration());
        MultiValueMap<String, String> formParameters = this.buildFormParameters(oAuth2JwtBearerGrantRequest);
        URI uri = UriComponentsBuilder.fromUriString(oAuth2JwtBearerGrantRequest.getClientRegistration().getProviderDetails().getTokenUri())
                .build()
                .toUri();
        log.info("URI for tokenEndpoint={}", uri.toString());
        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
    }

    private MultiValueMap<String, String> buildFormParameters(OAuth2JwtBearerGrantRequest oAuth2JwtBearerGrantRequest) {
        ClientRegistration clientRegistration = oAuth2JwtBearerGrantRequest.getClientRegistration();

        String scope = String.join(" ",
                Optional.ofNullable(clientRegistration.getScopes())
                .orElseThrow(() ->
                        new RuntimeException("scope must be set for client with registrationId="
                                + clientRegistration.getRegistrationId())));

        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        if (ClientAuthenticationMethod.POST.equals(clientRegistration.getClientAuthenticationMethod())) {
            formParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
            formParameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, oAuth2JwtBearerGrantRequest.getGrantType().getValue());
        formParameters.add(OAuth2ParameterNames.SCOPE, scope);
        formParameters.add(OAUTH2_PARAMETER_NAME_ASSERTION, oAuth2JwtBearerGrantRequest.getAssertion());
        formParameters.add(OAUTH2_PARAMETER_NAME_REQUESTED_TOKEN_USE, "on_behalf_of");
        return formParameters;
    }

    private HttpHeaders getTokenRequestHeaders(ClientRegistration clientRegistration) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final MediaType contentType = MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setContentType(contentType);
        if (ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
        }
        return headers;
    }
}
