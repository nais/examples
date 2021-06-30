package no.nav.security.examples.springsecurity.oauth2;

import no.nav.security.examples.springsecurity.ApiOnBehalfOfSpringSecurityApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ApiOnBehalfOfSpringSecurityApplication.class})
@ActiveProfiles("local")
class OnBehalfOfTokenResponseClientIT {

    private OnBehalfOfTokenResponseClient onBehalfOfTokenResponseClient;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        onBehalfOfTokenResponseClient = new OnBehalfOfTokenResponseClient(new RestTemplateBuilder());
    }

    @Test
    @Disabled("you must add a valid access token in assertion variable to run this test")
    void getTokenResponse() {
        String assertion = "INSERT VALID ASSERTION";
        ClientRegistration clientRegistration =
                clientRegistrationRepository.findByRegistrationId("example-onbehalfof");
        OAuth2AccessTokenResponse response =
                onBehalfOfTokenResponseClient.getTokenResponse(new OAuth2JwtBearerGrantRequest(clientRegistration, assertion));

        assertThat(response.getAccessToken()).isNotNull();
    }
}