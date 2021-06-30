package no.nav.security.examples.springsecurity.client;

import no.nav.security.examples.springsecurity.DaemonClientCredentialsSpringSecurityApplication;
import no.nav.security.examples.springsecurity.config.SecurityConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@EnableWebSecurity
@SpringBootTest(classes = SecurityConfiguration.class)
class DownstreamResourceClientTest {

    private static final String TOKEN = "tokenvalue";

    @Autowired
    private RestTemplate restTemplate;
    @MockBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @Mock
    private OAuth2AuthorizedClient authorizedClient;
    private DownstreamResourceClient downstreamResourceClient;
    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.server = new MockWebServer();
        this.server.start();

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(
                OAuth2AccessTokenResponse
                        .withToken(TOKEN)
                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                        .build().getAccessToken());
        downstreamResourceClient = new DownstreamResourceClient(
                server.url("/").toString(), restTemplate);
    }

    @AfterEach
    void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    void testPingWithClientCredentials() throws InterruptedException {
        server.enqueue(new MockResponse().setBody("pong"));
        assertThat(downstreamResourceClient.ping()).isNotBlank();
        RecordedRequest recordedRequest = this.server.takeRequest();
        assertThat(recordedRequest.getHeader("Authorization")).contains(TOKEN);
    }
}