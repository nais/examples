package no.nav.security.examples.tokensupport.config;

import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@Configuration
class OAuth2Configuration {

    /**
     * Create one RestTemplate per OAuth2 client entry to separate between different scopes per API
     */
    @Bean("azure")
    RestTemplate downstreamResourceRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                         ClientConfigurationProperties clientConfigurationProperties,
                                         OAuth2AccessTokenService oAuth2AccessTokenService) {

        ClientProperties clientProperties =
                Optional.ofNullable(clientConfigurationProperties.getRegistration().get("example-onbehalfof"))
                        .orElseThrow(() -> new RuntimeException("could not find oauth2 client config for example-onbehalfof"));
        return restTemplateBuilder
                .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
                .build();
    }

    @Bean("tokenx")
    RestTemplate downstreamResourceTokenXRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                                ClientConfigurationProperties clientConfigurationProperties,
                                                OAuth2AccessTokenService oAuth2AccessTokenService) {

        ClientProperties clientProperties =
                Optional.ofNullable(clientConfigurationProperties.getRegistration().get("example-tokenexchange"))
                        .orElseThrow(() -> new RuntimeException("could not find oauth2 client config for example-tokenexchange"));
        return restTemplateBuilder
                .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
                .build();
    }

    private ClientHttpRequestInterceptor bearerTokenInterceptor(ClientProperties clientProperties,
                                                                OAuth2AccessTokenService oAuth2AccessTokenService) {
        return (request, body, execution) -> {
            OAuth2AccessTokenResponse response =
                    oAuth2AccessTokenService.getAccessToken(clientProperties);
            request.getHeaders().setBearerAuth(response.getAccessToken());
            return execution.execute(request, body);
        };
    }
}

@Configuration
@Profile("local")
@PropertySource("classpath:application-local.secrets.properties")
class LocalProfileConfiguration {

}
