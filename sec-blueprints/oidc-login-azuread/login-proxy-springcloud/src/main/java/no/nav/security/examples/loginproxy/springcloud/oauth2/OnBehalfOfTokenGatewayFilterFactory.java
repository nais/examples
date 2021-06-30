package no.nav.security.examples.loginproxy.springcloud.oauth2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

@Component
public class OnBehalfOfTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<OnBehalfOfTokenGatewayFilterFactory.Config> {

    final Logger log = LoggerFactory.getLogger(OnBehalfOfTokenGatewayFilterFactory.class);

    private final OnBehalfOfTokenResponseClient client;
    private final ReactiveClientRegistrationRepository repository;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public OnBehalfOfTokenGatewayFilterFactory(
            OnBehalfOfTokenResponseClient client,
            ReactiveClientRegistrationRepository repository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        super(Config.class);
        this.client = client;
        this.repository = repository;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("clientRegistrationId");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> exchange.getPrincipal()
                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(authentication -> authorizedClient(exchange, authentication))
                .map(OAuth2AuthorizedClient::getAccessToken)
                .flatMap(token -> {
                    log.debug(String.format(
                            "getting on-behalf-of token for request path '%s' with config '%s'",
                            exchange.getRequest().getPath(),
                            config
                    ));

                    return retrieveOnBehalfOfToken(config, token);
                })
                .switchIfEmpty(Mono.error(couldNotRetrieveOnBehalfOfToken(config)))
                .map(token -> {
                    exchange.getRequest()
                            .mutate()
                            .headers(h -> h.setBearerAuth(token.getTokenValue()));

                    log.debug("attaching bearer token to request");

                    return exchange;
                })
                .flatMap(chain::filter);
    }

    private Mono<OAuth2AuthorizedClient> authorizedClient(ServerWebExchange exchange,
                                                          OAuth2AuthenticationToken oauth2Authentication) {
        return this.authorizedClientRepository.loadAuthorizedClient(
                oauth2Authentication.getAuthorizedClientRegistrationId(),
                oauth2Authentication, exchange);
    }

    private Mono<OAuth2AccessToken> retrieveOnBehalfOfToken(Config config, OAuth2AccessToken accessToken) {
        return Mono.defer(() -> this.repository.findByRegistrationId(config.clientRegistrationId)
                .map(clientRegistration -> new OAuth2JwtBearerGrantRequest(clientRegistration, accessToken.getTokenValue()))
                .flatMap(client::getTokenResponse)
                .map(OAuth2AccessTokenResponse::getAccessToken)
                .switchIfEmpty(Mono.error(clientRegistrationNotFound(config.clientRegistrationId)))
        );
    }

    private Supplier<? extends Throwable> couldNotRetrieveOnBehalfOfToken(Config config) {
        return () -> new RuntimeException("could not retrieve on-behalf-of access_token for config " + config);
    }

    private Supplier<? extends Throwable> clientRegistrationNotFound(String id) {
        return () -> new IllegalArgumentException("Could not find ClientRegistration with id '" + id + "'");
    }

    @Data
    @NoArgsConstructor
    public static class Config {
        private String clientRegistrationId;
    }
}



