package no.nav.security.examples.springsecurity.oauth2;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static no.nav.security.examples.springsecurity.oauth2.OAuth2JwtBearerGrantRequest.JWT_BEARER_GRANT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

class OnBehalfOfTokenResponseClientTest {

    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String DOWNSTREAM_API_SCOPE = "api://a1fd9dc1-2590-4e10-86a1-bc611c96dc17/.default";

    private OnBehalfOfTokenResponseClient onBehalfOfTokenResponseClient;
    private int port;
    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        server = new MockWebServer();
        server.start();
        port = server.getPort();
        onBehalfOfTokenResponseClient =
                new OnBehalfOfTokenResponseClient(new RestTemplateBuilder());
    }

    @AfterEach
    void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    void getTokenResponse() throws InterruptedException {
        String accessTokenSuccessResponse = "{\n" +
                "	\"access_token\": \"access-token-1234\",\n" +
                "   \"token_type\": \"bearer\",\n" +
                "   \"expires_in\": \"3600\",\n" +
                "   \"scope\": \"" + DOWNSTREAM_API_SCOPE + "\",\n" +
                "   \"custom_parameter_1\": \"custom-value-1\",\n" +
                "   \"custom_parameter_2\": \"custom-value-2\"\n" +
                "}\n";

        server.enqueue(jsonResponse(accessTokenSuccessResponse));

        ClientRegistration clientRegistration = aadClientRegistration();
        String assertion = "access-token-1";
        OAuth2JwtBearerGrantRequest jwtBearerGrantRequest = new OAuth2JwtBearerGrantRequest(clientRegistration,
                assertion);

        Instant expiresAtBefore = Instant.now().plusSeconds(3600);

        OAuth2AccessTokenResponse response =
                onBehalfOfTokenResponseClient.getTokenResponse(jwtBearerGrantRequest);

        Instant expiresAtAfter = Instant.now().plusSeconds(3600);

        RecordedRequest recordedRequest = this.server.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.toString());
        assertThat(recordedRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        String formParameters = recordedRequest.getBody().readUtf8();
        assertThat(formParameters).contains("grant_type=" + URLEncoder.encode(
                "urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8));
        assertThat(formParameters).contains("scope=" + URLEncoder.encode(DOWNSTREAM_API_SCOPE, StandardCharsets.UTF_8));
        assertThat(formParameters).contains("requested_token_use=on_behalf_of");
        assertThat(formParameters).contains("assertion=" + assertion);

        assertThat(response.getAccessToken().getTokenValue()).isEqualTo("access-token-1234");
        assertThat(response.getAccessToken().getTokenType()).isEqualTo(OAuth2AccessToken.TokenType.BEARER);
        assertThat(response.getAccessToken().getExpiresAt()).isBetween(expiresAtBefore, expiresAtAfter);
        assertThat(response.getAccessToken().getScopes()).containsExactly(DOWNSTREAM_API_SCOPE);
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getAdditionalParameters().size()).isEqualTo(2);
        assertThat(response.getAdditionalParameters()).containsEntry("custom_parameter_1", "custom-value-1");
        assertThat(response.getAdditionalParameters()).containsEntry("custom_parameter_2", "custom-value-2");
    }

    private MockResponse jsonResponse(String json) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json);
    }

    private ClientRegistration aadClientRegistration() {
        return ClientRegistration.withRegistrationId("aad-obo")
                .clientId("N/A for test")
                .clientSecret("N/A for test")
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(JWT_BEARER_GRANT_TYPE)
                .tokenUri("http://localhost:" + port + TOKEN_ENDPOINT)
                .clientName("Azure AD OBO")
                .scope(DOWNSTREAM_API_SCOPE)
                .build();
    }
}