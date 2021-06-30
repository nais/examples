package no.nav.security.examples.loginproxy.springcloud.oauth2;

import com.nimbusds.oauth2.sdk.GrantType;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class OAuth2JwtBearerGrantRequest extends AbstractOAuth2AuthorizationGrantRequest {

    public static final AuthorizationGrantType JWT_BEARER_GRANT_TYPE = new AuthorizationGrantType(GrantType.JWT_BEARER.getValue());
    private final ClientRegistration clientRegistration;
    private final String assertion;

    public OAuth2JwtBearerGrantRequest(ClientRegistration clientRegistration, String assertion) {
        super(JWT_BEARER_GRANT_TYPE);
        this.clientRegistration = clientRegistration;
        this.assertion = assertion;
    }

    public ClientRegistration getClientRegistration(){
        return this.clientRegistration;
    }

    public String getAssertion(){
        return this.assertion;
    }
}

