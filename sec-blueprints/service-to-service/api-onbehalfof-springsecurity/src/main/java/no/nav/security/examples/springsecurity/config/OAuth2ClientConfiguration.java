package no.nav.security.examples.springsecurity.config;

import no.nav.security.examples.springsecurity.oauth2.OnBehalfOfTokenOAuth2ClientProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OAuth2ClientConfiguration {

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .provider(new OnBehalfOfTokenOAuth2ClientProvider())
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    RestTemplate downstreamResourceRestTemplate(OAuth2AuthorizedClientManager authorizedClientManager){
        return new RestTemplateBuilder()
                .additionalInterceptors(bearerToken("example-onbehalfof", authorizedClientManager))
                .build();
    }

    private ClientHttpRequestInterceptor bearerToken(String clientRegistrationId,
                                                     OAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, body, execution) -> {

            OAuth2AuthorizedClient oAuth2AuthorizedClient = authorizedClientManager
                    .authorize(OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientRegistrationId)
                            .principal(SecurityContextHolder.getContext().getAuthentication())
                            .build());

            OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
            request.getHeaders().setBearerAuth(accessToken.getTokenValue());
            return execution.execute(request, body);
        };
    }
}
