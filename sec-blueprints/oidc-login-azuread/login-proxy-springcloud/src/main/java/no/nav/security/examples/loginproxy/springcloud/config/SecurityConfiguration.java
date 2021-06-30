package no.nav.security.examples.loginproxy.springcloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver.DEFAULT_AUTHORIZATION_REQUEST_PATTERN;
import static org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver.DEFAULT_REGISTRATION_ID_URI_VARIABLE_NAME;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private static final String LOGIN_PAGE_PATH = "/login";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, OAuth2ClientProperties clientProperties) {
        return http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .oauth2Client()
                .and()
                .oauth2Login()
                .and()
                //To avoid HTML page displaying all OAuth2 ClientRegistrations
                .addFilterAt(
                        redirectToDefaultClientRegistration(clientProperties),
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }

    @Bean
    WebClient client() {
        return WebClient.builder().build();
    }

    private WebFilter redirectToDefaultClientRegistration(OAuth2ClientProperties clientProperties) {
        var matcher = new PathPatternParserServerWebExchangeMatcher(LOGIN_PAGE_PATH);
        var authorizationRedirectStrategy = new DefaultServerRedirectStrategy();

        return (exchange, chain) -> matcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .map(it -> defaultLoginClientRegistrationId(clientProperties))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .map(registrationId ->
                        UriComponentsBuilder
                        .fromUriString(DEFAULT_AUTHORIZATION_REQUEST_PATTERN)
                        .buildAndExpand(Map.of(DEFAULT_REGISTRATION_ID_URI_VARIABLE_NAME, registrationId)).toUri()
                )
                .flatMap(redirectUri -> authorizationRedirectStrategy.sendRedirect(exchange, redirectUri));
    }

    private Optional<String> defaultLoginClientRegistrationId(OAuth2ClientProperties clientProperties) {
        return clientProperties.getRegistration().entrySet().stream()
                .filter(entry ->
                        entry.getValue().getAuthorizationGrantType().equals(
                                AuthorizationGrantType.AUTHORIZATION_CODE.getValue()
                        )
                ).map(Map.Entry::getKey).findFirst();
    }
}
