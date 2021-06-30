package no.nav.security.examples.springsecurity.oauth2;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


public class OnBehalfOfTokenOAuth2ClientProvider implements OAuth2AuthorizedClientProvider {
    //TODO remove restTemplateBuilder from client
    private OAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> accessTokenResponseClient =
            new OnBehalfOfTokenResponseClient(new RestTemplateBuilder());
    private Duration clockSkew = Duration.ofSeconds(60);
    private Clock clock = Clock.systemUTC();

    /**
     * Attempt to authorize the {@link OAuth2AuthorizationContext#getClientRegistration() client} in the provided
     * {@code context}.
     * Returns {@code null} if re-authorization is not supported,
     * e.g. the client is not authorized OR the {@link OAuth2AuthorizedClient#getAccessToken()} access token}
     * is not available for the authorized client OR the {@link OAuth2AuthorizedClient#getAccessToken() access token}
     * is expired.
     *
     * <p>
     * The following {@link OAuth2AuthorizationContext#getAttributes() context attributes} are supported:
     * <ol>
     *  <li>{@link OAuth2AuthorizationContext#REQUEST_SCOPE_ATTRIBUTE_NAME} (optional) - a {@code String[]} of scope(s)
     *  	to be requested by the {@link OAuth2AuthorizationContext#getClientRegistration() client}</li>
     * </ol>
     *
     * @param context the context that holds authorization-specific state for the client
     * @return the {@link OAuth2AuthorizedClient} or {@code null} if re-authorization is not supported
     */
    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");

        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if (authorizedClient != null && !hasTokenExpired(authorizedClient.getAccessToken())) {
            // If client is already authorized but access token is NOT expired than no need for re-authorization
            return null;
        }
        OAuth2JwtBearerGrantRequest jwtBearerGrantRequest = new OAuth2JwtBearerGrantRequest(
                context.getClientRegistration(), jwtAuthenticationToken(context).getToken().getTokenValue());
        OAuth2AccessTokenResponse tokenResponse =
                this.accessTokenResponseClient.getTokenResponse(jwtBearerGrantRequest);

        return new OAuth2AuthorizedClient(context.getClientRegistration(),
                context.getPrincipal().getName(), tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    private boolean hasTokenExpired(AbstractOAuth2Token token) {
        return token.getExpiresAt().isBefore(Instant.now(this.clock).minus(this.clockSkew));
    }

    /**
     * Sets the client used when requesting an access token credential at the Token Endpoint for the {@code jwt
     * -bearer} grant.
     *
     * @param accessTokenResponseClient the client used when requesting an access token credential at the Token
     *                                  Endpoint for the {@code jwt-bearer} grant
     */
    public void setAccessTokenResponseClient(OAuth2AccessTokenResponseClient<OAuth2JwtBearerGrantRequest> accessTokenResponseClient) {
        Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    /**
     * Sets the maximum acceptable clock skew, which is used when checking the
     * {@link OAuth2AuthorizedClient#getAccessToken() access token} expiry. The default is 60 seconds.
     * An access token is considered expired if it's before {@code Instant.now(this.clock) - clockSkew}.
     *
     * @param clockSkew the maximum acceptable clock skew
     */
    public void setClockSkew(Duration clockSkew) {
        Assert.notNull(clockSkew, "clockSkew cannot be null");
        Assert.isTrue(clockSkew.getSeconds() >= 0, "clockSkew must be >= 0");
        this.clockSkew = clockSkew;
    }

    /**
     * Sets the {@link Clock} used in {@link Instant#now(Clock)} when checking the access token expiry.
     *
     * @param clock the clock
     */
    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }

    public JwtAuthenticationToken jwtAuthenticationToken(OAuth2AuthorizationContext context) {
        return Optional.ofNullable(context.getPrincipal())
                .filter(o -> o instanceof JwtAuthenticationToken)
                .map(JwtAuthenticationToken.class::cast)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error(
                                "assertion_not_found",
                                "no assertion found for authenticated subject.", null)));
    }
}
