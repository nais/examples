package no.nav.security.examples.springsecurity.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken(
            "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    /***
     * Just here to allow unprotected access to the restcontroller to trigger Client Credentials Flow
     * Should not be implemented like this in production, usually an app in requirement for Client Credentials
     * is an app without HTTP endpoints like a daemon or e.g. kafka consumer
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/unprotected/**")
                .permitAll()
                .anyRequest()
                .fullyAuthenticated();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    RestTemplate downstreamResourceRestTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new RestTemplateBuilder()
                .interceptors(bearerToken("example-clientcredentials", authorizedClientManager))
                .build();

    }

    private ClientHttpRequestInterceptor bearerToken(String clientRegistrationId,
                                                     OAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, body, execution) -> {
            OAuth2AccessToken accessToken = authorizedClientManager
                    .authorize(OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientRegistrationId)
                            .principal(ANONYMOUS_AUTHENTICATION)
                            .build()).getAccessToken();
            request.getHeaders().setBearerAuth(accessToken.getTokenValue());
            return execution.execute(request, body);
        };
    }
}

@Configuration
@Profile("local")
@PropertySource("classpath:application-local.secrets.properties")
class LocalProfileConfiguration {

}
