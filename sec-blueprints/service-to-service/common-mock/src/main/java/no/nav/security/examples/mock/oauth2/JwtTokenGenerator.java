package no.nav.security.examples.mock.oauth2;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JwtTokenGenerator {

    /**
     * Example:
     *
     * aud: "a1fd9dc1-2590-4e10-86a1-bc611c96dc17",
     * iss: "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0",
     * iat: 1570127503,
     * nbf: 1570127503,
     * exp: 1570131403,
     * aio: "42VgYBC1myL/j7d2n1HfYn2thGXvAQ==",
     * azp: "863d14f3-68cb-411b-838f-acbedbbea447",
     * azpacr: "1",
     * oid: "c06528e4-24a5-46c8-8d10-19a3059d2ffd",
     * sub: "c06528e4-24a5-46c8-8d10-19a3059d2ffd",
     * tid: "966ac572-f5b7-4bbe-aa88-c76419c0f851",
     * uti: "7Oqg1Df9V0Ss3s1Tm5EFAA",
     * ver: "2.0"
     */
    public JWTClaimsSet aadClientCredentialsAccessTokenV2(String clientId, Map<String, String> formParameters, long expiry){
        Date now = new Date();
        String sub = UUID.randomUUID().toString();
        return new JWTClaimsSet.Builder()
                .audience("TODO")
                .issuer("")
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(new Date(now.getTime() + expiry))
                .claim("aio", UUID.randomUUID().toString())
                .claim("azp", clientId)
                .claim("azpacr", "1")
                .claim("oid", sub)
                .subject(sub)
                .claim("tid", UUID.randomUUID().toString())
                .claim("uti", UUID.randomUUID().toString())
                .claim("ver", "2.0")
                .build();
    }

    public static SignedJWT createSignedJWT(RSAKey rsaJwk, JWTClaimsSet claimsSet) {
        try {
            Builder header = new Builder(JWSAlgorithm.RS256)
                    .keyID(rsaJwk.getKeyID())
                    .type(JOSEObjectType.JWT);

            SignedJWT signedJWT = new SignedJWT(header.build(), claimsSet);
            JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
            signedJWT.sign(signer);

            return signedJWT;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
